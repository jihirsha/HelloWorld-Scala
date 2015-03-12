package hello;

import org.springframework.security.crypto.codec.Base64;

import java.util.ArrayList;

public class BasicAuth{

    public static Boolean checkURL(String str)
    {
        String strUrl = str;
        if(strUrl!=null && strUrl.startsWith("Basic")) {
            String credentials = strUrl.substring("Basic".length()).trim();
            String afterConversion = new String(Base64.decode(credentials.getBytes()));
            String[] values = afterConversion.split(":", 2);
            if (values[0].equals("foo") && values[1].equals("bar")) {
                return true;
            }
        }
        return false;
    }

}