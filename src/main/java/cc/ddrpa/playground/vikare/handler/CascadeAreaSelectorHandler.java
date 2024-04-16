package cc.ddrpa.playground.vikare.handler;

import java.util.List;

public class CascadeAreaSelectorHandler {
    private static String getLastObjectOfList(List<String> data) {
        return data.get(data.size() - 1);
    }

    private static String getDivisionLevelFrom(List<String> data) {
        var areaSplit = data.get(data.size() - 1).split("-");
        var areaCode = areaSplit[areaSplit.length - 1];
        switch (areaCode.length()) {
            case 2:
                return "6";
            case 4:
                return "5";
            case 6:
                return "4";
            case 9:
                return "3";
            case 12:
                return "2";
            case 15:
                return "1";
            default:
                throw new IllegalArgumentException("unknown");
        }
    }
}