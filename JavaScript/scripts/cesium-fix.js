const fs = require('fs');
const fileName = require.resolve('cesium/package.json');
// try to load the file
try {
  const jsonString = fs.readFileSync(fileName);
  const file = JSON.parse(jsonString);
  // add new field for proper exporting widgets.css
  file.exports["./Source/Widgets/widgets.css"] = "./Source/Widgets/widgets.css";
  // write the file
  fs.writeFile(fileName, JSON.stringify(file), function writeJSON(err) {
    if (err) return console.log(err);
    console.log('writing to ' + fileName);
  });
} catch (err) {
  console.log(err);
  return;
};