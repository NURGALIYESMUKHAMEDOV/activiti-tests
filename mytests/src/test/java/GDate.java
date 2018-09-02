import org.activiti.engine.*;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.LongFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;
import java.util.Random;

public class GDate {
    @Test
    public void GenerateRandomDate(){
        int dd=0, mm=0, gg=0;
        Random random = new Random();
        gg= random.nextInt(2019);
        mm= random.nextInt(13 - 1);
        if(mm != 2) {
            if (mm % 2 == 0 || mm == 7) {
                dd = random.nextInt(32 - 1);
            } else {
                dd = random.nextInt(31 - 1);
            }
        } else{
            if(gg%4==0){
                dd = 29;
            } else {
                dd = 28;
            }
        }

        String date= dd + "" + mm + gg;
        System.out.println(date);
    }
}