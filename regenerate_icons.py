from pathlib import Path
from PIL import Image, ImageDraw
base_dir = Path('app/src/main/res')
sizes = {'mipmap-mdpi':48,'mipmap-hdpi':72,'mipmap-xhdpi':96,'mipmap-xxhdpi':144,'mipmap-xxxhdpi':192}
for folder,size in sizes.items():
    path = base_dir / folder
    img = Image.new('RGBA',(size,size),(25,28,47,255))
    draw = ImageDraw.Draw(img)
    shield = [
        (size*0.5, size*0.18),
        (size*0.82, size*0.85),
        (size*0.68, size*0.85),
        (size*0.58, size*0.65),
        (size*0.42, size*0.65),
        (size*0.32, size*0.85),
        (size*0.18, size*0.85)
    ]
    draw.polygon(shield, fill=(255,255,255,255))
    inner = [
        (size*0.5, size*0.32),
        (size*0.62, size*0.82),
        (size*0.55, size*0.82),
        (size*0.5, size*0.66),
        (size*0.45, size*0.82),
        (size*0.38, size*0.82)
    ]
    draw.polygon(inner, fill=(25,28,47,255))
    draw.rectangle((size*0.36, size*0.63, size*0.64, size*0.71), fill=(255,179,0,255))
    draw.polygon([(size*0.5, size*0.22), (size*0.58, size*0.38), (size*0.42, size*0.38)], fill=(255,236,179,160))
    img.convert('RGB').save(path/'ic_launcher.png')
    mask = Image.new('L',(size,size),0)
    ImageDraw.Draw(mask).ellipse((0,0,size,size), fill=255)
    round_img = Image.new('RGBA',(size,size),(0,0,0,0))
    round_img.paste(img, (0,0), mask)
    round_img.convert('RGB').save(path/'ic_launcher_round.png')
