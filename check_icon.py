from PIL import Image
img = Image.open('app/src/main/res/mipmap-xxhdpi/ic_launcher.png')
print('size', img.size)
coords = [(10,70),(60,40),(72,100),(70,60),(30,100)]
for c in coords:
    print(c, img.getpixel(c))
