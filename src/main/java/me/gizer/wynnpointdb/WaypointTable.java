package me.gizer.wynnpointdb;

import com.wynntils.core.framework.rendering.colors.CustomColor;
import com.wynntils.modules.map.instances.WaypointProfile;
import me.gizer.wynnpointdb.db.Connector;
import me.gizer.wynnpointdb.db.Database;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class WaypointTable extends Database {
    private static final Encoder<WaypointProfile> WaypointEncoder = (statement, element) -> {
        statement.setString(1, element.getName());
        statement.setDouble(2, element.getX());
        statement.setDouble(3, element.getY());
        statement.setDouble(4, element.getZ());
        statement.setInt(5, element.getZoomNeeded());
        statement.setInt(6, element.getColor().toInt());
        statement.setInt(7, WaypointProfile.WaypointType.typetoint.get(element.getType()));
        statement.setInt(8, WaypointProfile.WaypointType.typetoint.get(element.getGroup()));
        statement.setTimestamp(9, Timestamp.from(Instant.now()));
    };

    private static final Decoder<WaypointProfile> WaypointDecoder = result -> {
        String name = result.getString(1);
        double x = result.getDouble(2);
        double y = result.getDouble(3);
        double z = result.getDouble(4);
        int zoom = result.getInt(5);
        CustomColor color = CustomColor.fromInt(result.getInt(6));
        WaypointProfile.WaypointType type = WaypointProfile.WaypointType.inttotype.get(result.getInt(7));
        WaypointProfile.WaypointType group = WaypointProfile.WaypointType.inttotype.get(result.getInt(8));

        WaypointProfile waypointProfile = new WaypointProfile(name, x, y, z, color, type, zoom);
        waypointProfile.setGroup(group);
        return waypointProfile;
    };

    public WaypointTable(Connector connector) {
        super(connector);
    }

    public void create() throws SQLException {
        update("CREATE TABLE IF NOT EXISTS tbl_waypoint (name VARCHAR(200), x double , y double, z double, zoom int, color int, type int, `group` int, firstFound TIMESTAMP, PRIMARY KEY (x,y,z))");
    }

    public void postlist(List<WaypointProfile> profiles) throws SQLException {
        update("REPLACE INTO tbl_waypoint VALUES (?,?,?,?,?,?,?,?,?)", Encoder.list(WaypointEncoder), profiles);
    }

    public List<WaypointProfile> getlist() throws SQLException {
        return query("SELECT * FROM tbl_waypoint", Decoder.list(WaypointDecoder));
    }
}
