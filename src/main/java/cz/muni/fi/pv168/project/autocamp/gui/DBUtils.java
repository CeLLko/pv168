/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.project.autocamp.gui;

import java.io.IOException;
import java.util.Properties;
import org.apache.derby.jdbc.ClientDataSource;

/**
 *
 * Adam Gdovin, 433305
 *
 * @version May 16, 2016
 */
public class DBUtils {

    public static ClientDataSource setDataSource() {

        Properties myconf = new Properties();
        try {
            myconf.load(AutoCampMenu.class.getClassLoader().getResourceAsStream("myconfig.properties"));
        } catch (IOException ex) {
            AutoCampMenu.logger.error("Problem occured while loading properties, " + ex);
            System.exit(-1);
        }

        ClientDataSource ds = new ClientDataSource();
        String host = myconf.getProperty("db.host");
        String name = myconf.getProperty("db.name");
        if (host == null) {
            AutoCampMenu.logger.error("Property db.host in myconfig.properties is not set.");
            System.err.println("ERROR: Property db.host in myconfig.properties is not set.");
            System.exit(-1);
        }
        if (name == null) {
            AutoCampMenu.logger.error("Property db.name in myconfig.properties is not set.");
            System.err.println("ERROR: Property db.name in myconfig.properties is not set.");
            System.exit(-1);
        }
        ds.setServerName(host);
        ds.setDatabaseName(name);
        try {
            ds.setPortNumber(Integer.parseInt(myconf.getProperty("db.port")));
        } catch (NumberFormatException ex) {
            AutoCampMenu.logger.error("Property db.port in myconfig.properties is either not set or not a valid number. [" + ex + "]");
            System.err.println("ERROR: Property db.port in myconfig.properties is either not set or not a valid number. [" + ex + "]");
            System.exit(-1);
        }        
        ds.setUser(myconf.getProperty("db.user"));
        ds.setPassword(myconf.getProperty("db.pass"));
        return ds;
    }
}
