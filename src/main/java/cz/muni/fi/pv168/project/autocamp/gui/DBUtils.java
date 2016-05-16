/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.project.autocamp.gui;

import cz.muni.fi.pv168.project.autocamp.gui.AutoCampMenu;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.derby.jdbc.ClientDataSource;

/**
 *
 * Adam Gdovin, 433305
 * @version May 16, 2016
 */
public class DBUtils {

    public static ClientDataSource setDataSource() {
        
        Properties myconf = new Properties();
        try {
            myconf.load(AutoCampMenu.class.getResourceAsStream("myconf.properties"));
        } catch (IOException ex) {
            Logger.getLogger(ex.toString());
        }
        
        ClientDataSource ds = new ClientDataSource();
        ds.setServerName(myconf.getProperty("db.host"));
        ds.setDatabaseName(myconf.getProperty("db.name"));
        ds.setPortNumber(Integer.parseInt(myconf.getProperty("db.port")));
        ds.setUser(myconf.getProperty("db.user"));
        ds.setPassword(myconf.getProperty("db.pass"));
        return ds;
    }
}
