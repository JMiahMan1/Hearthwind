#!/usr/bin/env python3
"""Minimal RCON client (Source RCON protocol) for headless server testing."""
import socket
import struct
import sys


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


def main():
    host, port, password = sys.argv[1], int(sys.argv[2]), sys.argv[3]
    with socket.create_connection((host, port), timeout=10) as s:
        s.sendall(pkt(1, 3, password))
        rid, _, _ = read_pkt(s)
        if rid == -1:
            sys.exit("auth failed")
        for i, cmd in enumerate(sys.argv[4:], start=10):
            s.sendall(pkt(i, 2, cmd))
            while True:
                rid, ptype, body = read_pkt(s)
                if ptype == 0 and rid == i:
                    print(f">>> {cmd}\n{body}")
                    break
                if ptype != 0:
                    continue


if __name__ == "__main__":
    main()
