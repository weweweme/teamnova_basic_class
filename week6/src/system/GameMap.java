package system;

import java.util.ArrayList;

/// <summary>
/// 게임 맵 전체를 관리하는 클래스
/// 120x40 크기의 타일 배열을 가지며, 각 타일의 이동 가능 여부를 관리
/// 맵 위의 자원 목록도 함께 관리
/// </summary>
public class GameMap {

    /// <summary>
    /// 맵 가로 크기 (문자 단위)
    /// </summary>
    public static final int WIDTH = 120;

    /// <summary>
    /// 맵 세로 크기 (문자 단위)
    /// </summary>
    public static final int HEIGHT = 40;

    /// <summary>
    /// 맵 전체 타일 배열 [행][열]
    /// </summary>
    private final Tile[][] tiles;

    /// <summary>
    /// 맵 위에 배치된 자원 목록
    /// </summary>
    private final ArrayList<Resource> resources = new ArrayList<>();

    /// <summary>
    /// 정착민 목록
    /// </summary>
    private final ArrayList<Colonist> colonists = new ArrayList<>();

    /// <summary>
    /// 식민지 공유 자원 보유량
    /// </summary>
    private final Supply supply = new Supply();

    /// <summary>
    /// 건설된 건물 목록
    /// </summary>
    private final ArrayList<Building> buildings = new ArrayList<>();

    /// <summary>
    /// 맵 생성, 모든 타일을 이동 가능한 평지로 초기화
    /// </summary>
    public GameMap() {
        tiles = new Tile[HEIGHT][WIDTH];
        for (int row = 0; row < HEIGHT; row++) {
            for (int col = 0; col < WIDTH; col++) {
                tiles[row][col] = new Tile(true);
            }
        }
    }

    /// <summary>
    /// 지정한 좌표가 맵 범위 안인지 확인
    /// </summary>
    public boolean isInBounds(int row, int col) {
        boolean validRow = row >= 0 && row < HEIGHT;
        boolean validCol = col >= 0 && col < WIDTH;
        return validRow && validCol;
    }

    /// <summary>
    /// 지정한 좌표의 타일 반환
    /// </summary>
    public Tile getTile(int row, int col) {
        return tiles[row][col];
    }

    /// <summary>
    /// 지정한 좌표로 이동할 수 있는지 확인
    /// 맵 범위 안이고, 해당 타일이 이동 가능해야 함
    /// </summary>
    public boolean isWalkable(int row, int col) {
        if (!isInBounds(row, col)) {
            return false;
        }
        return tiles[row][col].isWalkable();
    }

    /// <summary>
    /// 맵에 자원 추가
    /// </summary>
    public void addResource(Resource resource) {
        resources.add(resource);
    }

    /// <summary>
    /// 맵의 자원 목록 반환
    /// </summary>
    public ArrayList<Resource> getResources() {
        return resources;
    }

    /// <summary>
    /// 지정한 좌표를 포함하는 4x2 블록 범위에 있는 자원 찾기
    /// 커서가 자원 블록 위에 있으면 해당 자원 반환, 없으면 null
    /// </summary>
    public Resource findResourceAt(int row, int col) {
        // 자원 블록 크기
        int blockWidth = 4;
        int blockHeight = 2;

        for (Resource resource : resources) {
            int resRow = resource.getPosition().getRow();
            int resCol = resource.getPosition().getCol();

            boolean inRow = row >= resRow && row < resRow + blockHeight;
            boolean inCol = col >= resCol && col < resCol + blockWidth;

            if (inRow && inCol) {
                return resource;
            }
        }
        return null;
    }

    /// <summary>
    /// 맵에서 자원 제거 (채집 완료 시)
    /// </summary>
    public void removeResource(Resource resource) {
        resources.remove(resource);
    }

    /// <summary>
    /// 식민지 공유 자원 보유량 반환
    /// </summary>
    public Supply getSupply() {
        return supply;
    }

    /// <summary>
    /// 맵에 건물 추가, 벽이면 해당 타일을 이동 불가로 변경
    /// </summary>
    public void addBuilding(Building building) {
        buildings.add(building);

        // 벽은 4x2 블록 전체를 이동 불가로 설정
        if (building.getType() == BuildingType.WALL) {
            int startRow = building.getPosition().getRow();
            int startCol = building.getPosition().getCol();

            for (int row = startRow; row < startRow + 2; row++) {
                for (int col = startCol; col < startCol + 4; col++) {
                    if (isInBounds(row, col)) {
                        tiles[row][col].setWalkable(false);
                    }
                }
            }
        }
    }

    /// <summary>
    /// 건물 목록 반환
    /// </summary>
    public ArrayList<Building> getBuildings() {
        return buildings;
    }

    /// <summary>
    /// 지정한 좌표 근처(맨해튼 거리)에 특정 종류의 건물이 있는지 확인
    /// </summary>
    public boolean hasBuildingNearby(int row, int col, BuildingType type, int range) {
        for (Building building : buildings) {
            if (building.getType() != type) {
                continue;
            }

            int buildRow = building.getPosition().getRow();
            int buildCol = building.getPosition().getCol();
            int distRow = Math.abs(row - buildRow);
            int distCol = Math.abs(col - buildCol);
            int distance = distRow + distCol;

            if (distance <= range) {
                return true;
            }
        }
        return false;
    }

    /// <summary>
    /// 맵에 정착민 추가
    /// </summary>
    public void addColonist(Colonist colonist) {
        colonists.add(colonist);
    }

    /// <summary>
    /// 정착민 목록 반환
    /// </summary>
    public ArrayList<Colonist> getColonists() {
        return colonists;
    }
}
