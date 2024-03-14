import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

class user{
    Scanner sc = new Scanner(System.in);
    public void username(){
        System.out.println("Enter UserName : ");
        String name = sc.nextLine(); // Read username
        System.out.println("Enter the password : ");
        String pass = sc.nextLine(); // Read password
    
        try {
            File file = new File("user_credentials.txt");
            Scanner scanner = new Scanner(file);
            boolean userExists = false;
    
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts[0].equals(name)) {
                    userExists = true;
                    break;
                }
            }
            scanner.close();
    
            if (userExists) {
                System.out.println("Already signed up, please login!!! ");
            } else {
                FileWriter writer = new FileWriter("user_credentials.txt", true);
                writer.write(name + "," + pass + "\n");
                writer.close();
                System.out.println("User signed up successfully.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    
    public void login(){
        System.out.println("Enter Username : ");
        String name1 = sc.nextLine();
        System.out.println("Enter the password you entered before : ");
        String pass1 = sc.nextLine();
        try {
           
            File file = new File("user_credentials.txt");
            Scanner scanner = new Scanner(file);
            boolean userFound = false;

            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
               
                if (parts[0].equals(name1) && parts[1].equals(pass1)) {
                    userFound = true;
                    break;
                }
            }
            scanner.close();

            if (userFound) {
                System.out.println("You have successfully logged in!");
            } else {
                System.out.println("Invalid username or password.");
            }
        } catch (FileNotFoundException e) {
            System.out.println("No users signed up yet.");
        }
    }
}
public class NameInterface {
    public static void main(String[] args) {
        @SuppressWarnings("resource")
        Scanner sc=new Scanner(System.in);
        System.out.println("~~~~~~~~~Marcus Website~~~~~~~~");
        System.out.println("1.Signup\n2.Login\n3.Exit");
        int ch = sc.nextInt();
        user obj = new user();
        while(true){

            switch (ch) {
                case 1:
                    obj.username();
                    
                    break;
                case 2:
                    obj.login();
                    break;
                default:
                    exit();
                    break;
            }
        }
        
    }
    private static void exit() {
        System.exit(0);    
    }
      

   
}
