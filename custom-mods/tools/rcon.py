#!/usr/bin/env python3
"""Minimal RCON client (Source RCON protocol) for headless server testing.

Deterministic behavior:
  - Connection/auth is retried for --connect-wait seconds (default 60) so a
    server still booting never causes a spurious failure.
  - A command whose response times out is reported as `<no response (timeout)>`
    and marked failed; it is NEVER re-sent (commands may not be idempotent).
    Verify state with a follow-up query command.
  - Exit codes: 0 ok, 1 command failed, 2 auth failed, 3 connect failed.
"""

import argparse
import socket
import struct
import sys
import time

AUTH_OK = 0
CMD_FAILED = 1
AUTH_FAILED = 2
CONNECT_FAILED = 3


def pkt(req_id: int, ptype: int, body: str) -> bytes:
    data = struct.pack("<ii", req_id, ptype) + body.encode("utf-8") + b"\x00\x00"
    return struct.pack("<i", len(data)) + data


def read_pkt(sock) -> tuple[int, int, str]:
    raw = sock.recv(4)
    if len(raw) < 4:
        raise EOFError("connection closed")
    (length,) = struct.unpack("<i", raw)
    data = b""
    while len(data) < length:
        chunk = sock.recv(length - len(data))
        if not chunk:
            raise EOFError("connection closed mid-packet")
        data += chunk
    rid, ptype = struct.unpack("<ii", data[:8])
    return rid, ptype, data[8:-2].decode("utf-8", "replace")


def connect_auth(host, port, password, deadline, timeout):
    while True:
        try:
            s = socket.create_connection((host, port), timeout=timeout)
            s.sendall(pkt(1, 3, password))
            rid, _, _ = read_pkt(s)
            if rid == -1:
                s.close()
                return None, AUTH_FAILED
            return s, AUTH_OK
        except OSError as e:
            if time.monotonic() >= deadline:
                print(f"rcon: connect to {host}:{port} failed: {e}", file=sys.stderr)
                return None, CONNECT_FAILED
            time.sleep(0.5)


def main():
    ap = argparse.ArgumentParser(description="Minimal Source-RCON client")
    ap.add_argument("host")
    ap.add_argument("port", type=int)
    ap.add_argument("password")
    ap.add_argument("commands", nargs="*")
    ap.add_argument("--connect-wait", type=float, default=60.0,
                    help="seconds to keep retrying connect+auth (default 60)")
    ap.add_argument("--timeout", type=float, default=15.0,
                    help="per-response socket timeout in seconds (default 15)")
    args = ap.parse_args()

    deadline = time.monotonic() + args.connect_wait
    sock, rc = connect_auth(args.host, args.port, args.password, deadline, args.timeout)
    if sock is None:
        return rc

    failed = 0
    with sock:
        for i, cmd in enumerate(args.commands, start=10):
            try:
                sock.sendall(pkt(i, 2, cmd))
                sock.settimeout(args.timeout)
                while True:
                    rid, ptype, body = read_pkt(sock)
                    if ptype == 0 and rid == i:
                        print(f">>> {cmd}\n{body}")
                        break
            except socket.timeout:
                failed += 1
                print(f">>> {cmd}\n<no response (timeout)>")
            except OSError as e:
                failed += 1
                print(f">>> {cmd}\n<rcon error: {e}>")
    return CMD_FAILED if failed else AUTH_OK


if __name__ == "__main__":
    sys.exit(main())
