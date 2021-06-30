package loadbalancerlab.shared;

import loadbalancerlab.loadbalancer.HashRingAngle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AngleDataProcessorTest {
    SortedMap<Integer, Map<Integer, List<HashRingAngle>>> angleHistory;
    AngleDataProcessor angleProcessor;
    int[] serverIds;
    Map<Integer, Integer> serverIdTable;
    int[] timestamps;
    List<List<Integer>> numAnglesMat = new ArrayList<>();
    int indexTime;
    int numAngles = 25;
    int maxAngle = 120;

    @BeforeEach
    public void setup() {
        serverIds = new int[] { 39, 18, 16, 5 };
        int[] serverIdsCopy = new int[serverIds.length];
        System.arraycopy(serverIds, 0, serverIdsCopy, 0, serverIds.length);
        Arrays.sort(serverIdsCopy);

        // table mapping server id to order when sorted in ascending order of ids
        serverIdTable = new HashMap<>();

        for (int i = 0; i < serverIds.length; i++)
            serverIdTable.put(serverIdsCopy[i], i);

        numAnglesMat.add(Arrays.asList(5, 4, 9));
        numAnglesMat.add(Arrays.asList(8, 13, 2, 8));
        numAnglesMat.add(Arrays.asList(4, 9, 7, 6));

        indexTime = (int)(System.currentTimeMillis() / 1_000);
        timestamps = new int[] { indexTime - 15, indexTime - 10, indexTime - 5 };

        angleHistory = new TreeMap<>();
        List<HashRingAngle> anglePool = new ArrayList<>();

        // pool of angles from which angle positions are chosen
        List<Integer> possibleAngles = new ArrayList<>();

        for (int i = 0; i <= maxAngle; i++) {
            possibleAngles.add(i);
        }

        Random rand = new Random();

        for (int i = 0; i < numAngles; i++) {
            // fill up angle pool with angles with randomly generated angles
            int randIdx = rand.nextInt(possibleAngles.size());
            int selectedAngle = possibleAngles.get(randIdx);
            // remove selected angle from possible angles to prevent duplicates HashRingAngles with the same angle
            possibleAngles.remove(randIdx);
            anglePool.add(new HashRingAngle(i, selectedAngle));
        }

        angleHistory.put(timestamps[0], new HashMap<>());
        angleHistory.put(timestamps[1], new HashMap<>());
        angleHistory.put(timestamps[2], new HashMap<>());

        angleProcessor = new AngleDataProcessor(angleHistory);

        // fill up angle history matrix
        for (int timestampIdx = 0; timestampIdx < numAnglesMat.size(); timestampIdx++) {
            Map<Integer, List<HashRingAngle>> timestampEntry = angleHistory.get(timestamps[timestampIdx]);

            for (int serverIdIdx = 0; serverIdIdx < numAnglesMat.get(timestampIdx).size(); serverIdIdx++) {
                numAngles = numAnglesMat.get(timestampIdx).get(serverIdIdx);
                List<HashRingAngle> angleList = new ArrayList<>();

                for (int i = 0; i < numAngles; i++) {
                    int randAngleIdx = rand.nextInt(anglePool.size());
                    angleList.add(anglePool.get(randAngleIdx));
                }

                int serverId = serverIds[serverIdIdx];

                timestampEntry.put(serverId, angleList);
            }
        }
    }

    @Nested
    @DisplayName("Test getNumAnglesByTime()")
    class TestGetNumAnglesByTime {
        String[][] processedResult;

        @BeforeEach
        public void setup() {
            processedResult = angleProcessor.getNumAnglesByTime();
        }

        @Test
        @DisplayName("result should have correct dimensions")
        public void resultShouldHaveCorrectDimensions() {
            assertEquals(4, processedResult.length);
            assertEquals(serverIds.length + 1, processedResult[0].length);
        }

        @Test
        @DisplayName("should have correct number of angles by server for each timestamp")
        public void shouldReturnNumberOfAnglesByServer() {
            for (int timestampIdx = 0; timestampIdx < timestamps.length; timestampIdx++) {
                for (int serverIdIdx = 0; serverIdIdx < numAnglesMat.get(timestampIdx).size(); serverIdIdx++) {
                    int serverId = serverIds[serverIdIdx];
                    int serverIdOrder = serverIdTable.get(serverId);

                    assertEquals(String.valueOf(numAnglesMat.get(timestampIdx).get(serverIdIdx)), processedResult[timestampIdx + 1][serverIdOrder + 1]);
                }
            }
        }

        @Test
        @DisplayName("Result should have correct number of entries")
        public void resultShouldHaveCorrectNumEntries() {
            assertEquals(3 + 1, processedResult.length);
        }

        @Test
        @DisplayName("Result should have correct number of columns")
        public void resultShouldHaveCorrectNumColumns() {
            assertEquals(serverIds.length + 1, processedResult[0].length);
        }

        @Test
        @DisplayName("header row should contain all ids of servers")
        public void headerRowShouldContainAllServerIds() {
            String[] headerRow = processedResult[0];

            for (int i = 0; i < serverIds.length; i++) {
                String serverId = String.valueOf(serverIds[i]);
                boolean contains = false;

                for (int j = 0; j < headerRow.length; j++) {
                    if (serverId.equals(headerRow[j])) {
                        contains = true;
                        break;
                    }
                }

                assertTrue(contains);
            }
        }

        @Test
        @DisplayName("header row should contain ids of all servers in ascending order")
        public void headerRowShouldContainIdsOfServers() {
            String[] headerRow = processedResult[0];

            for (int i = 1; i < headerRow.length - 1; i++)
                assertTrue(Integer.parseInt(headerRow[i + 1]) >= Integer.parseInt(headerRow[i]));
        }

        @Test
        @DisplayName("leftmost column should contain all timestamps")
        public void leftMostColumnShouldContainTimestamps() {
            for (int i = 0; i < timestamps.length; i++) {
                int timestamp = timestamps[i];
                boolean contains = false;

                for (int row = 1; row < processedResult.length; row++) {
                    if (timestamp == Integer.parseInt(processedResult[row][0])) {
                        contains = true;
                        break;
                    }
                }

                assertTrue(contains);
            }
        }

        @Test
        @DisplayName("leftmost column should contain timestamps in ascending order")
        public void leftMostColumnShouldContainTimestampsAscending() {
            for (int row = 1; row < processedResult.length - 1; row++) {
                assertTrue(Integer.parseInt(processedResult[row][0]) <= Integer.parseInt(processedResult[row + 1][0]));
            }
        }
    }

    @Nested
    @DisplayName("Test getTotalSweepAngleByServerByTime()")
    class TestTotalSweepAngleByTime {
        String[][] processedResult;
    }
}
