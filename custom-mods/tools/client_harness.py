import argparse
import glob
import json
import os
import pathlib
import re
import subprocess
import sys
import time

REPO = pathlib.Path(__file__).resolve().parents[2]
SCRATCH = REPO / ".tmp"
GAME_DIR = REPO / "dev-client" / "client"
SERVER_DIR = REPO / "dev-server"
MODS_DIR = REPO / "dev-client" / "mods"
SHOTS_DIR = SCRATCH / "shots"
STDOUT_LOG = SCRATCH / "logs" / "client-stdout.log"
RCON_TOOL = REPO / "custom-mods" / "tools" / "rcon.py"
RCON_HOST, RCON_PORT, RCON_PASS = "127.0.0.1", "25575", "agedtest"

CLIENT_PID_PAT = "[K]notClient"
SERVER_PID_PAT = "[f]abric-server.jar"

LOG_FILES = {
    "client": GAME_DIR / "logs" / "latest.log",
    "server": SERVER_DIR / "logs" / "latest.log",
    "stdout": STDOUT_LOG,
}


def sh(cmd, **kw):
    return subprocess.run(cmd, capture_output=True, text=True, check=False, **kw)


def mc_client_pid():
    r = sh(["pgrep", "-f", CLIENT_PID_PAT])
    return int(r.stdout.split()[0]) if r.stdout.strip() else None


def window_geom():
    pid = mc_client_pid()
    if not pid:
        raise RuntimeError("Minecraft client not running")
    script = (
        f'tell application "System Events" to tell (first process whose unix id is {pid}) '
        f"to get {{position, size}} of front window"
    )
    r = sh(["osascript", "-e", script])
    if r.returncode != 0:
        raise RuntimeError(f"osascript failed: {r.stderr.strip()}")
    nums = [int(float(n)) for n in re.findall(r"-?\d+(?:\.\d+)?", r.stdout)]
    if len(nums) < 4:
        raise RuntimeError(f"unexpected window geometry: {r.stdout.strip()!r}")
    return nums[0], nums[1], nums[2], nums[3]


def build_classpath():
    vjson = json.loads(
        (pathlib.Path.home() / "Library/Application Support/minecraft/versions/26.2/26.2.json").read_text()
    )
    libs_root = pathlib.Path.home() / "Library/Application Support/minecraft/libraries"

    def rule_ok(rules):
        if not rules:
            return True
        allowed = False
        for r in rules:
            feats = r.get("features")
            if feats and not all(feats.values()):
                continue
            os_c = r.get("os", {})
            if os_c.get("name") not in (None, "osx"):
                continue
            arch = os_c.get("arch")
            if arch and arch != os.uname().machine:
                continue
            allowed = r["action"] == "allow"
        return allowed

    cp = []
    for lib in vjson.get("libraries", []):
        if not rule_ok(lib.get("rules")):
            continue
        art = lib.get("downloads", {}).get("artifact")
        if art and art.get("path"):
            p = libs_root / art["path"]
            if p.exists():
                cp.append(str(p))
        for cls in ("natives-macos", "natives-macos-arm64"):
            c = lib.get("downloads", {}).get("classifiers", {}).get(cls)
            if c and c.get("path"):
                p = libs_root / c["path"]
                if p.exists():
                    cp.append(str(p))
    cache = pathlib.Path.home() / ".gradle/caches/modules-2/files-2.1"

    def first(pat):
        hits = sorted(glob.glob(str(cache / pat)))
        if not hits:
            raise RuntimeError(f"gradle cache miss: {pat}")
        return hits[0]

    loader = first("net.fabricmc/fabric-loader/*/*/fabric-loader-*.jar")
    mixin = first("net.fabricmc/sponge-mixin/*/*/sponge-mixin-*.jar")
    mixex = first("io.github.llamalad7/mixinextras-fabric/*/*/mixinextras-fabric-*.jar")
    asm = ""
    for name in ("asm", "asm-tree", "asm-commons", "asm-util", "asm-analysis"):
        asm += first(f"org.ow2.asm/{name}/9*/*/asm*9*.jar") + ":"
    game_jar = str(
        pathlib.Path.home() / "Library/Application Support/minecraft/versions/26.2/26.2.jar"
    )
    cp.append(game_jar)
    SCRATCH.mkdir(parents=True, exist_ok=True)
    (SCRATCH / "mc_cp.txt").write_text(":".join(cp))
    return loader, mixin, mixex, asm, ":".join(cp), game_jar, vjson["assetIndex"]["id"]


def cmd_launch(args):
    old = mc_client_pid()
    if old:
        sh(["kill", str(old)])
        for _ in range(40):
            time.sleep(0.5)
            if not mc_client_pid():
                break
    opts = GAME_DIR / "options.txt"
    lines = []
    if opts.exists():
        lines = [ln for ln in opts.read_text(errors="replace").splitlines()
                 if ln and not ln.startswith(("onboardAccessibility:", "pauseOnLostFocus:"))]
    lines += ["onboardAccessibility:false", "pauseOnLostFocus:false"]
    opts.write_text("\n".join(lines) + "\n")
    loader, mixin, mixex, asm, mccp, game_jar, asset_idx = build_classpath()
    cmd = [
        "java", "-Xmx2G", "-XstartOnFirstThread",
        "--enable-native-access=ALL-UNNAMED",
        "--sun-misc-unsafe-memory-access=allow",
        f"-Dfabric.gameJarPath={game_jar}",
        "-cp", f"{loader}:{mixin}:{mixex}:{asm}{mccp}",
        "net.fabricmc.loader.impl.launch.knot.KnotClient",
        "--username", args.username,
        "--version", "26.2",
        "--gameDir", str(GAME_DIR),
        "--assetsDir", str(pathlib.Path.home() / "Library/Application Support/minecraft/assets"),
        "--assetIndex", asset_idx,
        "--uuid", "00000000-0000-0000-0000-000000000000",
        "--accessToken", "0",
        "--versionType", "Hearthwind",
    ]
    if args.quickplay:
        cmd += ["--quickPlayMultiplayer", f"{args.host}:{args.port}", "--server", args.host, "--port", args.port]
    logf = STDOUT_LOG.open("w")
    subprocess.Popen(cmd, stdout=logf, stderr=subprocess.STDOUT, stdin=subprocess.DEVNULL,
                     cwd=str(GAME_DIR), start_new_session=True)
    print(f"launched pid -> log {STDOUT_LOG}")


def cmd_shot(args):
    SHOTS_DIR.mkdir(parents=True, exist_ok=True)
    out = SHOTS_DIR / f"{args.name}.png"
    win = mc_window()
    if win:
        _, wid, _, _, _, _ = win
        r = sh(["screencapture", "-x", "-o", f"-l{wid}", str(out)])
        if r.returncode == 0 and out.exists():
            print(out)
            return
    x, y, w, h = window_geom()
    sh(["screencapture", "-x", f"-R{x},{y},{w},{h}", str(out)])
    print(out)


def cmd_waitwindow(args):
    deadline = time.time() + args.timeout
    while time.time() < deadline:
        win = mc_window()
        if win:
            print(f"window {win}")
            return
        time.sleep(1)
    sys.exit(1)


def cmd_activate(args):
    win = mc_window()
    if not win:
        sys.exit(1)
    script = (
        f'tell application "System Events" to set frontmost of '
        f'(first process whose unix id = {win[0]}) to true'
    )
    r = sh(["osascript", "-e", script])
    sys.exit(r.returncode or 0)


def cmd_dismiss(args):
    win = mc_window()
    if not win:
        sys.exit(1)
    pid, _, x, y, w, h = win
    script = (
        f'tell application "System Events" to set frontmost of '
        f'(first process whose unix id = {pid}) to true'
    )
    sh(["osascript", "-e", script])
    time.sleep(1.5)
    cx = x + w // 2
    cy = y + int(h * 0.938)
    sh(["cliclick", f"c:{cx},{cy}"])
    print(f"clicked {cx},{cy}")
    time.sleep(1.5)
    out = SHOTS_DIR / f"{args.name}.png" if args.name else None
    if out:
        w2 = mc_window()
        if w2:
            sh(["screencapture", "-x", "-o", f"-l{w2[1]}", str(out)])
            print(out)


def cmd_click(args):
    r = sh(["cliclick", f"c:{args.x},{args.y}"])
    sys.exit(r.returncode or 0)


def cmd_move(args):
    r = sh(["cliclick", f"m:{args.x},{args.y}"])
    sys.exit(r.returncode or 0)


def cmd_key(args):
    r = sh(["cliclick", f"kp:{args.key}"])
    sys.exit(r.returncode or 0)


def cmd_type(args):
    r = sh(["cliclick", "w:80", f"t:{args.text}"])
    sys.exit(r.returncode or 0)


HOLD_HELPER = SCRATCH / "bin" / "hw-cghold"

WINLIST_SRC = """import CoreGraphics
import Foundation
let opts: CGWindowListOption = [.optionAll]
let raw = CGWindowListCopyWindowInfo(opts, kCGNullWindowID) as! [[String: Any]]
for w in raw {
    let owner = w["kCGWindowOwnerName"] as? String ?? "?"
    if owner.contains("java") {
        let pid = w["kCGWindowOwnerPID"] as? Int ?? -1
        let wid = w["kCGWindowNumber"] as? Int ?? -1
        let name = w["kCGWindowName"] as? String ?? "?"
        let b = w["kCGWindowBounds"] as? [String: Any] ?? [:]
        let on = w["kCGWindowIsOnscreen"] as? Bool ?? false
        let layer = w["kCGWindowLayer"] as? Int ?? -99
        print("pid=\\(pid) id=\\(wid) name=\\(name) onscreen=\\(on) layer=\\(layer) " +
              "X=\\(b["X"] ?? 0) Y=\\(b["Y"] ?? 0) W=\\(b["Width"] ?? 0) H=\\(b["Height"] ?? 0)")
    }
}
"""


def ensure_winlist():
    WINLIST = SCRATCH / "bin" / "winlist"
    WINLIST.parent.mkdir(parents=True, exist_ok=True)
    if not WINLIST.exists():
        src = SCRATCH / "bin" / "winlist.swift"
        src.write_text(WINLIST_SRC)
        r = sh(["swiftc", "-O", "-o", str(WINLIST), str(src)])
        if r.returncode != 0:
            raise RuntimeError(f"swiftc failed: {r.stderr[-1500:]}")
    return WINLIST


def mc_window():
    out = sh([str(ensure_winlist())]).stdout
    for ln in out.splitlines():
        if "name=Minecraft" not in ln:
            continue

        def g(k, ln=ln):
            m = re.search(rf'"{k}": (-?\d+)', ln) or re.search(rf"{k}=(-?\d+)", ln)
            return int(m.group(1)) if m else None

        pidm = re.search(r"pid=(\d+)", ln)
        widm = re.search(r"(?:^|\s)id=(\d+)", ln)
        x, y, w, h = g("X"), g("Y"), g("Width"), g("Height")
        if not (pidm and widm) or None in (x, y, w, h):
            continue
        return int(pidm.group(1)), int(widm.group(1)), x, y, w, h
    return None


def ensure_hold_helper():
    HOLD_HELPER.parent.mkdir(parents=True, exist_ok=True)
    if HOLD_HELPER.exists():
        return HOLD_HELPER
    src = SCRATCH / "bin" / "hw-cghold.swift"
    src.write_text(
        "import CoreGraphics\nimport Foundation\n"
        "let a = CommandLine.arguments\n"
        "let x = Double(a[1])!\nlet y = Double(a[2])!\n"
        "let ms = useconds_t((Double(a[3]) ?? 200) * 1000)\n"
        "let pt = CGPoint(x: x, y: y)\n"
        "let isLeft = a.count > 3 && a[3] == \"left\"\n"
        "let b: CGMouseButton = isLeft ? .left : .right\n"
        "func post(_ t: CGEventType, _ b: CGMouseButton) {\n"
        "  CGEvent(mouseEventSource: nil, mouseType: t, mouseCursorPosition: pt, mouseButton: b)!"
        ".post(tap: .cghidEventTap)\n}\n"
        "post(.mouseMoved, b)\nusleep(50000)\n"
        "if isLeft {\n"
        "  post(.leftMouseDown, b)\n usleep(ms)\n  post(.leftMouseUp, b)\n"
        "} else {\n"
        "  post(.rightMouseDown, b)\n usleep(ms)\n  post(.rightMouseUp, b)\n"
        "}\n"
    )
    r = sh(["swiftc", "-O", "-o", str(HOLD_HELPER), str(src)])
    if r.returncode != 0:
        raise RuntimeError(f"swiftc failed: {r.stderr[-2000:]}")
    return HOLD_HELPER


def cmd_rhold(args):
    helper = ensure_hold_helper()
    r = sh([str(helper), str(args.x), str(args.y), str(args.ms), args.button])
    sys.exit(r.returncode or 0)


def cmd_keyhold(args):
    osa = (
        f'tell application "System Events" to key down "{args.key}"\n'
        f"delay {args.ms / 1000.0}\n"
        f'tell application "System Events" to key up "{args.key}"'
    )
    r = sh(["osascript", "-e", osa])
    sys.exit(r.returncode or 0)


def cmd_rcon(args):
    r = sh([sys.executable, str(RCON_TOOL), RCON_HOST, RCON_PORT, RCON_PASS, args.command])
    print(r.stdout.strip())
    sys.exit(r.returncode or 0)


def cmd_log(args):
    path = LOG_FILES[args.file]
    if not path.exists():
        print(f"(no log yet: {path})")
        return
    lines = path.read_text(errors="replace").splitlines()
    if args.pattern:
        rx = re.compile(args.pattern)
        lines = [ln for ln in lines if rx.search(ln)]
    tail = lines[-args.tail:]
    print("\n".join(tail))


def wait_log(fname, pattern, timeout):
    path = LOG_FILES[fname]
    rx = re.compile(pattern)
    deadline = time.time() + timeout
    while time.time() < deadline:
        if path.exists():
            try:
                if any(rx.search(ln) for ln in path.read_text(errors="replace").splitlines()[-4000:]):
                    return True
            except OSError:
                pass
        time.sleep(0.5)
    return False


def cmd_wait(args):
    ok = wait_log(args.file, args.pattern, args.timeout)
    print("MATCH" if ok else "TIMEOUT")
    sys.exit(0 if ok else 1)


def cmd_window(args):
    x, y, w, h = window_geom()
    print(json.dumps({"x": x, "y": y, "w": w, "h": h}))


def run_scenario(steps):
    failures = []
    for i, step in enumerate(steps):
        label = f"step[{i}] {json.dumps(step)[:120]}"
        try:
            if "sleep" in step:
                time.sleep(float(step["sleep"]))
            elif "rcon" in step:
                r = sh([sys.executable, str(RCON_TOOL), RCON_HOST, RCON_PORT, RCON_PASS, step["rcon"]])
                out = r.stdout.strip()
                print(f"  rcon> {out.splitlines()[-1] if out else '(no output)'}")
                if r.returncode != 0:
                    failures.append(f"{label}: rcon exit {r.returncode}")
            elif "shot" in step:
                x, y, w, h = window_geom()
                out = SHOTS_DIR / f"{step['shot']}.png"
                sh(["screencapture", "-x", f"-R{x},{y},{w},{h}", str(out)])
                print(f"  shot> {out}")
            elif "click_center" in step:
                x, y, w, h = window_geom()
                sh(["cliclick", f"c:{x + w // 2},{y + h // 2}"])
            elif "click" in step:
                cx, cy = step["click"]
                sh(["cliclick", f"c:{cx},{cy}"])
            elif "move" in step:
                mx, my = step["move"]
                sh(["cliclick", f"m:{mx},{my}"])
            elif "key" in step:
                sh(["cliclick", f"kp:{step['key']}"])
            elif "type" in step:
                sh(["cliclick", "w:80", f"t:{step['type']}"])
            elif "rhold" in step:
                rx, ry, ms = step["rhold"]
                sh([str(ensure_hold_helper()), str(rx), str(ry), str(ms)])
            elif "keyhold" in step:
                k, ms = step["keyhold"]
                osa = (
                    f'tell application "System Events" to key down "{k}"\n'
                    f"delay {ms / 1000.0}\n"
                    f'tell application "System Events" to key up "{k}"'
                )
                sh(["osascript", "-e", osa])
            elif "wait_log" in step:
                w_ = step["wait_log"]
                ok = wait_log(w_["file"], w_["pattern"], w_.get("timeout", 30))
                print(f"  wait_log> {'MATCH' if ok else 'TIMEOUT'} {w_['pattern']}")
                if not ok:
                    failures.append(f"{label}: wait_log timeout")
            elif "assert_log" in step:
                a_ = step["assert_log"]
                path = LOG_FILES[a_["file"]]
                text = path.read_text(errors="replace") if path.exists() else ""
                found = re.search(a_["pattern"], text) is not None
                want = a_.get("present", True)
                print(f"  assert_log> {'OK' if found == want else 'FAIL'} {a_['pattern']}")
                if found != want:
                    failures.append(f"{label}: assert_log {a_['pattern']}")
            else:
                failures.append(f"{label}: unknown step keys {list(step)}")
        except (OSError, RuntimeError, ValueError, KeyError, TypeError) as e:
            failures.append(f"{label}: {e}")
    if failures:
        print("SCENARIO FAILURES:")
        for f in failures:
            print("  -", f)
        sys.exit(1)
    print("SCENARIO PASSED")


def cmd_scenario(args):
    steps = json.loads(pathlib.Path(args.file).read_text())
    run_scenario(steps)


def main():
    ap = argparse.ArgumentParser(description="Hearthwind live client test harness")
    sub = ap.add_subparsers(dest="cmd", required=True)

    p = sub.add_parser("launch")
    p.add_argument("--quickplay", action="store_true")
    p.add_argument("--host", default="localhost")
    p.add_argument("--port", default="25565")
    p.add_argument("--username", default="TestPlayer")
    p.set_defaults(fn=cmd_launch)

    p = sub.add_parser("shot")
    p.add_argument("name")
    p.set_defaults(fn=cmd_shot)

    p = sub.add_parser("click")
    p.add_argument("x", type=int)
    p.add_argument("y", type=int)
    p.set_defaults(fn=cmd_click)

    p = sub.add_parser("move")
    p.add_argument("x", type=int)
    p.add_argument("y", type=int)
    p.set_defaults(fn=cmd_move)

    p = sub.add_parser("key")
    p.add_argument("key")
    p.set_defaults(fn=cmd_key)

    p = sub.add_parser("type")
    p.add_argument("text")
    p.set_defaults(fn=cmd_type)

    p = sub.add_parser("rhold")
    p.add_argument("x", type=int)
    p.add_argument("y", type=int)
    p.add_argument("--ms", type=int, default=2200)
    p.add_argument("--button", default="right", choices=["left", "right"])
    p.set_defaults(fn=cmd_rhold)

    p = sub.add_parser("keyhold")
    p.add_argument("key")
    p.add_argument("--ms", type=int, default=1000)
    p.set_defaults(fn=cmd_keyhold)

    p = sub.add_parser("rcon")
    p.add_argument("command")
    p.set_defaults(fn=cmd_rcon)

    p = sub.add_parser("log")
    p.add_argument("file", choices=list(LOG_FILES))
    p.add_argument("--pattern")
    p.add_argument("--tail", type=int, default=30)
    p.set_defaults(fn=cmd_log)

    p = sub.add_parser("wait")
    p.add_argument("file", choices=list(LOG_FILES))
    p.add_argument("pattern")
    p.add_argument("--timeout", type=int, default=30)
    p.set_defaults(fn=cmd_wait)

    p = sub.add_parser("window")
    p.set_defaults(fn=cmd_window)

    p = sub.add_parser("waitwindow")
    p.add_argument("--timeout", type=int, default=90)
    p.set_defaults(fn=cmd_waitwindow)

    p = sub.add_parser("activate")
    p.set_defaults(fn=cmd_activate)

    p = sub.add_parser("dismiss")
    p.add_argument("name", nargs="?")
    p.set_defaults(fn=cmd_dismiss)

    p = sub.add_parser("scenario")
    p.add_argument("file")
    p.set_defaults(fn=cmd_scenario)

    args = ap.parse_args()
    args.fn(args)


if __name__ == "__main__":
    main()
