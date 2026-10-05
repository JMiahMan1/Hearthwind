# Java 25, matching MC 26.2's declared java_version - Prism auto-provisions
# 25 for real players (see update_prism.sh), so testing on 26 proved nothing
# about the runtime users actually get: a C2 compiler crash on Microsoft
# JDK 25.0.1 shipped through CI green for that reason. Keep this at 25.
FROM eclipse-temurin:25-jdk-noble

# Headless server-gametest image: dedicated MC server runs (fabric-server.jar
# downloaded at runtime by the harness) plus the python static checks.
# The repo subset is mounted at run time; no repo contents bake in.
RUN apt-get update && apt-get install -y --no-install-recommends \
      curl python3 ca-certificates \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /work/repo/custom-mods
CMD ["bash", "tools/run_gametests.sh"]
