/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.project.autocamp.gui;
    
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * Adam Gdovin, 433305
 * @version May 16, 2016
 */
public class LocalizationWizard {
    private static final String DEFAULT_SETTINGS = "default_locales";

    public static String getString(String key) {
        try{
            return ResourceBundle.getBundle(DEFAULT_SETTINGS+"_"+Locale.getDefault().toString()).getString(key);
        } catch(Exception ex){
            return ResourceBundle.getBundle(DEFAULT_SETTINGS).getString(key);
        }
    }
}
