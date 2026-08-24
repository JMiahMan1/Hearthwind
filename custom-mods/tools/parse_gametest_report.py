#!/usr/bin/env python3
"""Parse a JUnit XML report from the fabric-api gametest harness.

Prints a one-line summary plus details for failures; exits nonzero if any
test failed or errored.
"""
import sys
import xml.etree.ElementTree as ET


def main(path: str) -> int:
    root = ET.parse(path).getroot()
    total = failures = errors = skipped = 0
    for case in root.iter("testcase"):
        name = f"{case.get('classname', '?')}.{case.get('name', '?')}"
        failure = case.find("failure")
        error = case.find("error")
        problem = failure if failure is not None else error
        if problem is not None:
            if problem.tag == "failure":
                failures += 1
            else:
                errors += 1
            print(f"[{problem.tag.upper()}] {name}")
            text = (problem.text or "").strip()
            for line in text.splitlines()[:10]:
                print(f"    {line}")
        elif case.find("skipped") is not None:
            skipped += 1
            print(f"[SKIP] {name}")
        total += 1
    passed = total - failures - errors - skipped
    print(f"\ngametests: {passed}/{total} passed"
          + (f", {failures} failed, {errors} errored" if failures or errors else ""))
    return 1 if failures or errors else 0


if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit(f"usage: {sys.argv[0]} <report.xml>")
    sys.exit(main(sys.argv[1]))
