package testeexecucaoarquivo;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Scanner;

public class TesteExecucaoArquivo {
    
    public static void main(String[] args) throws FileNotFoundException, IOException {        
        System.out.println(System.getProperty("user.dir"));
        Process p = Runtime.getRuntime().exec("/home/rodrigo/NetBeansProjects/TesteExecucaoArquivo/src/testeexecucaoarquivo/a.sh");
        
        FileReader file = new FileReader("./src/GRAPH.txt");
        Scanner scanner = new Scanner(file);
        
        scanner.next();
        while(scanner.hasNext()) {
            System.out.println(scanner.nextLine());
        }
    }
    
}
