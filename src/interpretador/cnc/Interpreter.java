package interpretador.cnc;
/**
 *
 * @author Luis
 */
public class Interpreter {
    
     private final String characters="0987654321,.-";
     private float x = 0, y= 0, z = 0;
     
     public void Interpret(String CommandLine)
     {
        float tempx = x, tempy = y, tempz = z;
        String lido = CommandLine;  
        if (lido.substring(0, 1).equals("N")) //inicia por N, linha valida
            {              
                if (lido.indexOf("Z") >= 0) //Encontra o valor de Z
                {
                    String vz = "";
                    int x = 0, localz = 0;
                    char[] clido = lido.toCharArray();
                    for (x=1;x<lido.length();x++) //define posição inicial onde comçar a ler valor do z
                    {
                        if(clido[x] == 'Z')
                        {
                            localz = x;
                            x = lido.length();
                        }
                    }       
                    for (x=localz+1;x<lido.length();x++)
                    {
                        if(characters.indexOf(String.valueOf(clido[x])) >= 0)
                            vz = vz + String.valueOf(clido[x]);  
                        else
                            x = lido.length();
                    }
                    tempz = Float.valueOf(vz);                   
                }
                 if (lido.indexOf("X") >= 0) //Encontra o valor de X
                {
                    String vx = "";
                    int x = 0, localx = 0;
                    char[] clido = lido.toCharArray();
                    for (x=1;x<lido.length();x++)
                    {
                        if(clido[x] == 'X')
                        {
                            localx = x;
                            x = lido.length();
                        }
                    }       
                    for (x=localx+1;x<lido.length();x++)
                    {
                        if(characters.indexOf(String.valueOf(clido[x])) >= 0)
                            vx = vx + String.valueOf(clido[x]);                                
                        else
                            x = lido.length();
                    }
                    tempx = Float.valueOf(vx);
                }
                if (lido.indexOf("Y") >= 0) //Encontra o valor de Y
                {
                    String vy = "";
                    int x = 0, localy = 0;
                    char[] clido = lido.toCharArray();
                    for (x=1;x<lido.length();x++)
                    {
                        if(clido[x] == 'Y')
                        {
                            localy = x;
                            x = lido.length();
                        }
                    }       
                    for (x=localy+1;x<lido.length();x++)
                    {
                        if(characters.indexOf(String.valueOf(clido[x])) >= 0)
                            vy = vy + String.valueOf(clido[x]);                                
                        else
                            x = lido.length();
                    }
                    tempy = Float.valueOf(vy);
                }          
                if (tempy < 0)
                    tempy = tempy *(-1);
                if (tempx < 0)
                    tempx = tempx *(-1);
                x = tempx;
                y = tempy;
                z = tempz;
            }        
     }
     public float GetX() 
     {
         return x;
     }
     public float GetY()
     {
         return y;   
     }
     public float GetZ()
     {
           if (z < 0 )
              return 1;
           else
              return 0;           
     //    return z;
     }
    
}
