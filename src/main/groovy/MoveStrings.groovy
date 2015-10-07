class Globals {
    static String FLAVOURS_FOLDER = "/src";
    static String FOLDER_SPLITTER = "/";
    static List<String> FILES_NAME = ['strings.xml', "strings-profile.xml"] as String[];
    static String RES_FOLDER_NAME = "/res";
    static String STRINGS_MAIN_FOLDER_NAME = "/values";
    static String STRINGS_DEFAULT_FOLDER_NAME = "/values-ru";
    static String STRING_RESOURCE_FORMAT = "string name=\"%s\"";
    static final HashMap<String, String> FLAVOURS_WITH_APP_NAME;
    static {
        HashMap<String, String> map = new HashMap<>();
        map.put("google", "Topface");
        map.put("blueStacks", "Spark");
        map.put("derived", "Alcatel");
        FLAVOURS_WITH_APP_NAME = Collections.unmodifiableMap(map);
    }

    static ArrayList<String> mFlavoursList;
    static ArrayList<String> mBuildTypesList;
    static String MAIN_FLAVOUR_NAME = "google";
    static HashMap<String, ArrayList<String>> mFlavourWithFolders;
}

// упорото привожу аргумент к списку (((
def saveFlavoursList(def flavours) {
    Globals.mFlavoursList = new ArrayList<>();
    println("arg[0] = " + flavours)
    String[] list = flavours.split(",")
    for (int i = 0; i < list.length; i++) {
        String value = getClearArrayElement(list[i])
        if (isValidArrayElement(value)) {
            Globals.mFlavoursList.add(value)
        }
        println("flavour #" + i + " - " + value)
    }
}

def saveBuildTypesList(def buildTypes) {
    Globals.mBuildTypesList = new ArrayList<>();
    println("arg[1] = " + buildTypes)
    String[] list = buildTypes.split(",")
    for (int i = 0; i < list.length; i++) {
        String value = getClearArrayElement(list[i])
        if (isValidArrayElement(value)) {
            Globals.mBuildTypesList.add(value)
        }
        println("build type #" + i + " - " + value)
    }
}

String getClearArrayElement(String value) {
    value = value.replaceAll(" ", "")
    value = value.replace("[", "")
    value = value.replace("]", "")
    return value;
}

boolean isValidArrayElement(String value) {
    return value != null && value.length() > 0 && value != "null";
}

def findMainFlavourName() {
    boolean isMainFlavourContains = false;
    for (int i = 0; i < Globals.mFlavoursList.size(); i++) {
        String currentFlavour = Globals.mFlavoursList.get(i);
        if (currentFlavour.equals(Globals.MAIN_FLAVOUR_NAME)) {
            isMainFlavourContains = true;
            break;
        }
    }
    if (!isMainFlavourContains) {
        println("Couldn't find main flavour " + Globals.MAIN_FLAVOUR_NAME);
        System.exit(1);
    }
}

ArrayList<String> getAllFoldersList() {
    ArrayList<String> foldersArray = new ArrayList<>();
    String path = new File(getClass().protectionDomain.codeSource.location.path).parent;
    String[] mainPath = path.split(Globals.FLAVOURS_FOLDER);
    if (mainPath.length == 0) {
        return foldersArray;
    }
    new File(mainPath[0] + Globals.FLAVOURS_FOLDER).eachDir() { dir ->
        foldersArray.add(dir.getPath());
    }
    return foldersArray;
}

def matchFlavoursWithFolders() {
    ArrayList<String> foldersArray = getAllFoldersList();
    for (int i = 0; i < foldersArray.size(); i++) {
        println("folder " + i + " : " + foldersArray.get(i));
    }
    fillFlavoursMap(foldersArray);
}

def fillFlavoursMap(ArrayList<String> foldersArray) {
    Globals.mFlavourWithFolders = new HashMap<String, ArrayList<String>>();
    for (int i = 0; i < Globals.mFlavoursList.size(); i++) {
        Globals.mFlavourWithFolders.put(Globals.mFlavoursList.get(i), getFoldersByFlavour(Globals.mFlavoursList.get(i), foldersArray));
    }
}


ArrayList<String> getFoldersByFlavour(String flavour, ArrayList<String> foldersArray) {
    ArrayList<String> folders = new ArrayList<>();
    for (int i = 0; i < foldersArray.size(); i++) {
        String folder = foldersArray.get(i);
        String flavourFolder = folder.split(Globals.FLAVOURS_FOLDER)[1].replaceAll(Globals.FOLDER_SPLITTER, "");
        if (flavourFolder.equals(flavour)) {
            folders.add(folder);
        } else {
            for (int j = 0; j < Globals.mBuildTypesList.size(); j++) {
                String buildType = Globals.mBuildTypesList.get(j);
                if (flavourFolder.toLowerCase().equals(flavour + buildType)) {
                    folders.add(folder);
                }
            }
        }
    }
    return folders;
}

def printMap() {
    for (String key : Globals.mFlavourWithFolders.keySet()) {
        ArrayList<String> folders = Globals.mFlavourWithFolders.get(key);
        for (int i = 0; i < folders.size(); i++) {
            println("flavour - " + key + "  folder: " + folders.get(i));
        }
    }
}

def compareSecondaryFlavourWithMain(String flavour) {

    for (String path : Globals.mFlavourWithFolders.get(flavour)) {
        for (String fileName : Globals.FILES_NAME) {
            String resXmlMainFlavour = new File(getPathToDefaultFile(Globals.mFlavourWithFolders.get(Globals.MAIN_FLAVOUR_NAME).get(0), fileName)).text;
            File resXml = new File(getPathToDefaultFile(path, fileName));
            if (resXml.isFile()) {
                def parse = new XmlParser().parse(resXml);
                parse.string.each { name ->
                    String key = String.format(Globals.STRING_RESOURCE_FORMAT, "${name.'@name'}");
                    if (!resXmlMainFlavour.contains(key)) {
                        println("\nОтсутствует строка ".concat(key).concat(" в flavour ").concat(Globals.MAIN_FLAVOUR_NAME).concat("\nПеренесите ее из flavour ").concat(flavour));
                        System.exit(1);
                    }
                }
            }
        }
    }
}

String getPathToDefaultFile(String flavourPath, String fileName) {
    return flavourPath.concat(Globals.RES_FOLDER_NAME).concat(Globals.STRINGS_DEFAULT_FOLDER_NAME).concat(Globals.FOLDER_SPLITTER).concat(fileName);
}

def findDifferencesOnSecondaryFlavours() {
    for (String flavour : Globals.mFlavoursList) {
        if (!flavour.equals(Globals.MAIN_FLAVOUR_NAME)) {
            compareSecondaryFlavourWithMain(flavour);
        }
    }
}

ArrayList<String> getAllResFolderfByFlavour(String flavour) {
    ArrayList<String> resultArray = new ArrayList<>();
    ArrayList<String> path = Globals.mFlavourWithFolders.get(flavour);
    for (String currentPath : path) {
        String path1 = currentPath.concat(Globals.RES_FOLDER_NAME);
        String name = Globals.STRINGS_MAIN_FOLDER_NAME.replaceAll(Globals.FOLDER_SPLITTER, "");
        new File(path1).eachDirMatch(~/${name}.*/) {
            file ->
                resultArray.add(file.getPath());
        }
    }
    return resultArray;
}

def deleteResFilesByFlavour(String flavour) {
    for (String path : getAllResFolderfByFlavour(flavour)) {
        for (String fileName : Globals.FILES_NAME) {
            File file = new File(path.concat(Globals.FOLDER_SPLITTER).concat(fileName));
            if (file != null && file.isFile()) {
                if (!file.delete()) {
                    println("WARNING. Error while " + file.getAbsoluteFile() + " delete");
                }
            }
        }
    }
}

def deleteResFilesAllSecondaryFlavours() {
    for (String flavour : Globals.mFlavoursList) {
        if (!flavour.equals(Globals.MAIN_FLAVOUR_NAME)) {
            deleteResFilesByFlavour(flavour);
        }
    }
}

def deleteAllEmptyDirByFlavour(String flavour) {
    def emptyDirs = [];
    for (String path : getAllResFolderfByFlavour(flavour)) {
        File dir = new File(path);
        if (dir.isDirectory() && (dir.list().length == 0)) {
            emptyDirs << dir
        }
    }
    emptyDirs.each { folder -> folder.delete() }
}

def deleteAllEmptyDirInSecondaryFlavours() {
    for (String flavour : Globals.mFlavoursList) {
        if (!flavour.equals(Globals.MAIN_FLAVOUR_NAME)) {
            deleteAllEmptyDirByFlavour(flavour);
        }
    }
}

String getPath() {
    return Globals.mFlavourWithFolders.get(Globals.MAIN_FLAVOUR_NAME).get(0).concat(Globals.RES_FOLDER_NAME).concat(Globals.STRINGS_DEFAULT_FOLDER_NAME).concat(Globals.FOLDER_SPLITTER).concat(Globals.FILES_NAME[0]);
}

def copyFilesFromMainFlavour() {
    for (String path : getAllResFolderfByFlavour(Globals.MAIN_FLAVOUR_NAME)) {
        for (String flavour : Globals.mFlavoursList) {
            if (!flavour.equals(Globals.MAIN_FLAVOUR_NAME)) {
                String destPath = path.replace(Globals.FOLDER_SPLITTER.concat(Globals.MAIN_FLAVOUR_NAME).concat(Globals.FOLDER_SPLITTER), Globals.FOLDER_SPLITTER.concat(flavour).concat(Globals.FOLDER_SPLITTER));
                new File(destPath).mkdir();
                for (String fileName : Globals.FILES_NAME) {
                    def src = new File(path.concat(Globals.FOLDER_SPLITTER).concat(fileName));
                    def dst = new File(destPath.concat(Globals.FOLDER_SPLITTER).concat(fileName))
                    dst << src.text
                }
            }
        }
    }
}

def replaceAppName() {
    for (String key : Globals.FLAVOURS_WITH_APP_NAME.keySet()) {
        if (!key.equals(Globals.MAIN_FLAVOUR_NAME)) {
            printRes(key, Globals.FLAVOURS_WITH_APP_NAME.get(Globals.MAIN_FLAVOUR_NAME));
        }
    }
}

def printRes(String flavour, String defaultName) {
    println("replace AppName flavour " + flavour + " from " + defaultName + " to " + Globals.FLAVOURS_WITH_APP_NAME.get(flavour));
    for (String path : getAllResFolderfByFlavour(flavour)) {
        for (String fileName : Globals.FILES_NAME) {
//            File file = new File(path.concat(Globals.FOLDER_SPLITTER).concat(fileName));
            String filePath = path.concat(Globals.FOLDER_SPLITTER).concat(fileName);
            Node parse = new XmlParser().parse(filePath);
//            while (parse.iterator().hasNext()){
//                parse.iterator().next()
//            }
            parse.string.each { name ->
                if (name.text().contains(defaultName)) {
                    parse.setValue(name.text().replace(defaultName, Globals.FLAVOURS_WITH_APP_NAME.get(flavour)));
                }
//                name.text().replace(defaultName, Globals.FLAVOURS_WITH_APP_NAME.get(flavour));
            }
            def writer = new FileWriter(filePath)
            new XmlNodePrinter(new PrintWriter(writer)).print(parse)
        }
    }

    /*
    теперь надо взять все файлы из данного флейвора (список дирректорий с ресурсами можно получить в методе getAllResFolderfByFlavour("имя флейвора"))
    пройтись по всем файлам в каждой из дирректорий
    файлы можно распарсить с помощью класса XmlParser

     def parse = new XmlParser().parse("путь к файлу");
                parse.string.each { name ->
                name.text() //вернет именно value
                // получили значение, нашли вхождение дефолтного названия App  и зареплейсили его именем для текущего флейвора
                }
    на данном этапе есть xml  со всеми необходимыми изменениями
    осталось только понять как их выгрузить в файл

    АХТУНГ только сейчас понял,что прохожусь в скрипте по строкам, но не трогаю plurals. Блин, печаль(((
     */

}

saveFlavoursList(args[0]);
saveBuildTypesList(args[1]);
findMainFlavourName();
matchFlavoursWithFolders();
findDifferencesOnSecondaryFlavours();
deleteResFilesAllSecondaryFlavours();
deleteAllEmptyDirInSecondaryFlavours();
copyFilesFromMainFlavour();
replaceAppName();