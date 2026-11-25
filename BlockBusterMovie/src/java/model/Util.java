package model;

public class Util {
    
    public static String generateCode(){
        
        int r = (int) (Math.random() * 1000000);
        return String.format("%06d", r);
    }
    
    public static boolean isEmailValid(String email){
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    }
    
    public static boolean isPasswordValid(String password){
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$");
    }
    
    public static boolean isCodeValid(String code){
        return code.matches("^\\d{4,5}$");
    }
    
    public static boolean isMobileValid(String mobile) {
        return mobile.matches("^07[01245678][0-9]{7}$");
    }

        
    public static boolean isInteger(String text){
        return text.matches("^-?\\d+$");
    }
    
    public static boolean isDouble(String text){
        return text.matches("^\\d+(\\.\\d{2})?$");
    }
    
    public static boolean isYouTubeLink(String url) {
        return url != null && url.matches("^(https?://)?(www\\.)?(youtube\\.com|youtu\\.be)/.+$");
    }

    
}
