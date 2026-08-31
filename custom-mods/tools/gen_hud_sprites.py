import pathlib

from PIL import Image, ImageDraw

ROOT = pathlib.Path(__file__).resolve().parents[1]
OUT = ROOT / "hearthwind-client" / "src" / "main" / "resources" / "assets" / "hearthwind" / "textures" / "gui" / "sprites" / "hud"


def outlined(size, fill, border=(20, 20, 20)):
    img = Image.new("RGBA", size, (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, size[0] - 1, size[1] - 1], fill=fill, outline=border)
    hi = tuple(min(255, c + 45) for c in fill[:3]) + (255,)
    if size[0] > 3 and size[1] > 3:
        d.line([1, 1, size[0] - 2, 1], fill=hi)
        d.line([1, 1, 1, size[1] - 2], fill=hi)
    return img


def droplet(fill, border=(20, 20, 20)):
    img = Image.new("RGBA", (9, 9), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([1, 3, 7, 8], fill=fill, outline=border)
    d.polygon([(4, 0), (6, 3), (2, 3)], fill=fill, outline=border)
    return img


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    sprites = {
        "nutrient_green": droplet((60, 200, 80)),
        "nutrient_darkgreen": droplet((30, 130, 50)),
        "nutrient_yellow": droplet((235, 200, 50)),
        "nutrient_red": droplet((220, 60, 50)),
        "nutrient_orange": droplet((240, 140, 40)),
        "nutrient_empty": droplet((70, 70, 70)),
        "temp_bar_empty": outlined((8, 9), (70, 70, 70)),
        "temp_cold": outlined((8, 9), (80, 150, 255)),
        "temp_warm": outlined((8, 9), (250, 120, 50)),
        "job_bg": outlined((182, 9), (30, 30, 36), border=(90, 90, 90)),
        "job_icon": outlined((9, 9), (230, 180, 60)),
        "job_xp": Image.new("RGBA", (91, 3), (100, 220, 100, 255)),
    }
    for name, img in sprites.items():
        img.save(OUT / f"{name}.png")

    strip = Image.new("RGBA", (45, 9), (0, 0, 0, 0))
    for i, color in enumerate(
        [(60, 200, 80), (30, 130, 50), (235, 200, 50), (220, 60, 50), (240, 140, 40)]
    ):
        strip.paste(outlined((9, 9), color), (i * 9, 0))
    (OUT.parent / "nutrients.png").parent.mkdir(parents=True, exist_ok=True)
    strip.save(OUT.parent / "nutrients.png")
    print(f"wrote {len(sprites) + 1} sprites to {OUT}")


if __name__ == "__main__":
    main()
