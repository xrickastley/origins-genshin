import fs from "fs";
import path from "path";

const folders = fs.readdirSync(__dirname)
	.filter(contents => !contents.includes(`.`))
	.sort((a, b) => assignCharacterPriority(b) - assignCharacterPriority(a));

const table: { character: string, name: string, filePath: string }[] = []

for (const folder of folders) {
	const files = fs.readdirSync(path.join(__dirname, folder))
		.filter(contents => contents.endsWith(`.png`))
		.sort((a, b) => assignPriority(a) - assignPriority(b));
	const character = normalizeName(folder);

	console.log(character);

	for (const file of files) {
		const name = folder === `passives`
			? normalizeName(file).replace(`.png`, ``)
			: file.startsWith(`passive`)
				? file.includes(`utility`)
					? `Utility Passive`
					: file.endsWith(`1.png`)
						? `1st Ascension Passive`
						: `4th Ascension Passive`
				: normalizeName(file).replace(`.png`, ``);

		const imgPath = `![file](../img/skills/${path.join(folder, file).replace(`\\`, `/`)})`;

		table.push({ character, name: `${name} ${imgPath}`, filePath: "origins-genshin:skills/" + path.join(folder, file).replace(`\\`, `/`) });
	}
}

const characterLength: number = table.reduce((prev, data) => Math.max(prev, data.character.length), 0) + 2;
const nameLength: number = table.reduce((prev, data) => Math.max(prev, data.name.length), 0) + 2;
const spriteLocationLength: number = table.reduce((prev, data) => Math.max(prev, data.filePath.length), 0) + 5;

table.unshift({ character: `-`.repeat(characterLength), name: `-`.repeat(nameLength), filePath: `-`.repeat(spriteLocationLength - 2) })
table.unshift({ character: fillExtraWithSpace(`Character`, characterLength), name: fillExtraWithSpace(`Skill Name`, nameLength), filePath: `Sprite Location` })

console.log(
	table
		.map((data, index) => `| ${fillExtraWithSpace(data.character, characterLength)}| ${fillExtraWithSpace(data.name, nameLength)}| ${fillExtraWithSpace(index <= 1 ? data.filePath : `\`${data.filePath}\``, spriteLocationLength)}|`)
		.join(`\n`)
)

function upperCaseFront(text: string) {
	return text[0].toUpperCase() + text.slice(1, text.length)
}

function normalizeName(name: string) {
	return name
		.split(`_`)
		.map(character => upperCaseFront(character))
		.join(' ')
}

function fillExtraWithSpace(text: string, length: number) {
	return text + " ".repeat(length - text.length);
}

function assignCharacterPriority(folder: string) {
	return folder === `passives` ? 0 : 1; 
}

function assignPriority(file: string) {
	if (file.toLowerCase().includes(`elemental`)) {
		if (file.toLowerCase().includes(`skill`)) {
			return 1;
		} else {
			return 2;
		}
	} else if (file.toLowerCase().includes(`passive`)) {
		return 3;
	} else {
		return 4;
	}
}