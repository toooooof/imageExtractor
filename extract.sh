echo "Starting image extraction"

java -cp out/production/extractor net.toooooof.Extractor $1 --tolerance 30 --search-boundaries-percentage 0.7 --starting-image-counter 12

dos2unix commands.sh
chmod +x commands.sh

echo "Cleaning former execution"
mkdir raw
rm raw/*

echo "Starting image rotation and cropping if possible"

./commands.sh

echo "Moving work images"
mv image*.png raw
mv rotated-*.png raw
mv cropped-*.png raw

echo "Done"
