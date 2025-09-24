/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg_expediciones;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author mmarco
 */
public class WriteFile {
    
    private String path;
    //booleano que usaremos para indicar si queremos
    //añadir texto al contenido ya existente o 
    //sobreescribir todo
    private boolean append_to_file = false; 
    
    public WriteFile (String file_path){
        path = file_path;
    }
    
    public WriteFile (String file_path, boolean append_value){
        path = file_path;
        append_to_file = append_value;
        
        
    }
    
    public void writeToFile (String textLine) throws IOException{
            //FileWriter write = new FileWriter (path, append_to_file);
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(path, append_to_file), StandardCharsets.UTF_8);
            PrintWriter print_line = new PrintWriter(write);
            
            print_line.printf("%s"+ "%n", textLine);
            print_line.close();
            
            
    }
}
