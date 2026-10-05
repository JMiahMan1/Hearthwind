# Java 25, matching MC 26.2's declared java_version - Prism auto-provisions
# 25 for real players (see update_prism.sh), so testing on 26 proved nothing
# about the runtime users actually get: a C2 compiler crash on Microsoft
# JDK 25.0.1 shipped through CI green for that reason. Keep this at 25.
FROM eclipse-temurin:25-jdk-noble

# Headless client gametest image: real MC client under xvfb with Mesa
# software GL. The repo is mounted at run time; no repo contents bake in.
# libxtst6/libxext are required by AWT (Fabric's error-dialog path loads
# libawt_xawt -> libXtst); without them a mod-resolution failure dies with
# UnsatisfiedLinkError and masks the real cause.
RUN apt-get update && apt-get install -y --no-install-recommends \
      xvfb libgl1 libglu1-mesa libxcursor1 libxrandr2 libxrender1 \
      libxi6 libxinerama1 libxxf86vm1 libxtst6 libxext6 \
      python3 ca-certificates \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /work/custom-mods
CMD ["bash", "tools/run_client_gametests.sh"]
