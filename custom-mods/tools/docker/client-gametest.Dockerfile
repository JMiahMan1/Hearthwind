FROM eclipse-temurin:26-jdk-noble

# Headless client gametest image: real MC client under xvfb with Mesa
# software GL. The repo is mounted at run time; no repo contents bake in.
RUN apt-get update && apt-get install -y --no-install-recommends \
      xvfb libgl1 libglu1-mesa libxcursor1 libxrandr2 libxrender1 \
      libxi6 libxinerama1 libxxf86vm1 python3 ca-certificates \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /work/custom-mods
CMD ["bash", "tools/run_client_gametests.sh"]
