package weakreference;

import java.lang.ref.WeakReference;

/** 
 * @author wison 
 */  
public class TestWeakReference {  


    public static void main(String[] args) {
        
        Car car = new Car(22000,"silver");
        WeakReference<Car> weakCar = new WeakReference<Car>(car);
        
        int i=0;
          
        while(true){
            if(weakCar.get()!=null) {
                i++;
                System.out.println("Object is alive for "+i+" loops - "+weakCar);
            } else {
                System.out.println("Object has been collected.");
                break;
            }
        }
    }
}  