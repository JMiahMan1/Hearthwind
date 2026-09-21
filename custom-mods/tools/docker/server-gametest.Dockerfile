FROM eclipse-temurin:26-jdk-noble

# Headless server-gametest image: dedicated MC server runs (fabric-server.jar
# downloaded at runtime by the harness) plus the python static checks.
# The repo subset is mounted at run time; no repo contents bake in.
RUN apt-get update && apt-get install -y --no-install-recommends \
      curl python3 ca-certificates \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /work/repo/custom-mods
CMD ["bash", "tools/run_gametests.sh"]
