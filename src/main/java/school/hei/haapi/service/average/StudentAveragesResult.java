package school.hei.haapi.service.average;

import java.util.Map;

public record StudentAveragesResult(
    Map<Integer, Double> perSemester,
    Map<String, Double> perYear,
    double overallCursusAverage,
    int creditsTaken,
    int expectedCredits,
    boolean eligibleForDiploma) {}
