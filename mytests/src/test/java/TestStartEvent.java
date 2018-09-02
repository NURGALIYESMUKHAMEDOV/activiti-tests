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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestStartEvent {

    private String dbName = "activiti2";
    private String xmlName = "startTest.bpmn20.xml";
    private String processDefinitionId = "teststartevent";


    private RuntimeService runtimeService = null;
    private ProcessEngineConfiguration cfg = null;
    private ProcessEngine processEngine = null;
    private RepositoryService repositoryService = null;
    private Deployment deployment = null;
    private TaskService taskService = null;
    private FormService formService = null;
    private HistoryService historyService = null;
    private ProcessInstance processInstance = null;
    private ProcessDefinition processDefinition = null;

    private String processId = null;
    @Before
    public void setUp() {
        cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:mysql://localhost:3306/" + dbName)
                .setJdbcUsername("root")
                .setJdbcPassword("")
                .setJdbcDriver("com.mysql.jdbc.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);


        processEngine = cfg.buildProcessEngine();
        String pName = processEngine.getName();
        String ver = ProcessEngine.VERSION;
        System.out.println("ProcessEngine [" + pName + "] Version: [" + ver + "]");

        repositoryService = processEngine.getRepositoryService();

        deployment = repositoryService.createDeployment().addClasspathResource(xmlName).deploy();
        processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
        System.out.println(
                "Found process definition ["
                        + processDefinition.getName() + "] with id ["
                        + processDefinition.getId() + "]");

        runtimeService = processEngine.getRuntimeService();
        processInstance = runtimeService.startProcessInstanceByKey(processDefinitionId);

        processId = processInstance.getProcessInstanceId();

        System.out.println("Onboarding process started with process instance id ["
                + processInstance.getProcessInstanceId()
                + "] key [" + processInstance.getProcessDefinitionKey() + "]");

        taskService = processEngine.getTaskService();
        formService = processEngine.getFormService();
        historyService = processEngine.getHistoryService();
    }

    @Test
    public void StartProcess() throws ParseException {

        Scanner scanner = new Scanner(System.in);

        while (processInstance != null && !processInstance.isEnded()) {

            List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();

            System.out.println("Активный тасктар: [" + tasks.size() + "]");

            for (int i = 0; i < tasks.size(); i++) {

                Task task = tasks.get(i);
                System.out.println("Орындалып жаткан таск [" + task.getName() + "]");
                Map<String, Object> variables = new HashMap<String, Object>();
                variables.put("birthDate", new Date());
                taskService.setVariables(task.getId(), variables);

            }

            printHistory();

            // Re-query the process instance, making sure the latest state is available
            processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstance.getId()).singleResult();

        }

        scanner.close();
    }

    //@After
    public void printHistory(){

        HistoricActivityInstance endActivity = null;

        List<HistoricActivityInstance> activities =
                historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(processInstance.getId()).finished()
                        .orderByHistoricActivityInstanceEndTime().asc()
                        .list();

        for (HistoricActivityInstance activity : activities) {
            if (activity.getActivityType().equals("startEvent")) {
                System.out.println("BEGIN " + processDefinition.getName()
                        + " [" + processInstance.getProcessDefinitionKey()
                        + "] " + activity.getStartTime());
            }

            if (activity.getActivityType().equals("userTask")) {
                TaskQuery query = taskService.createTaskQuery().processInstanceId(processId);

                List<Task> tasks = query.list();
                System.out.println("tasks.size: " + tasks.size());
                for(Task task : tasks){

                    FormData formData = formService.getTaskFormData(task.getId());
                    for (FormProperty formProperty : formData.getFormProperties()) {
                        System.out.println(formProperty.getName() + ": " + formProperty.getValue());
                    }

                }

            }

            if (activity.getActivityType().equals("endEvent")) {
                // Handle edge case where end step happens so fast that the end step
                // and previous step(s) are sorted the same. So, cache the end step
                //and display it last to represent the logical sequence.
                endActivity = activity;

            } else {
                System.out.println("-- " + activity.getActivityName()
                        + " [" + activity.getActivityId() + "] "
                        + activity.getDurationInMillis() + " ms"
                        + " type: " + activity.getActivityType());
            }
        }

        if (endActivity != null) {
            System.out.println("-- " + endActivity.getActivityName()
                    + " [" + endActivity.getActivityId() + "] "
                    + endActivity.getDurationInMillis() + " ms");
            System.out.println("COMPLETE " + processDefinition.getName() + " ["
                    + processInstance.getProcessDefinitionKey() + "] "
                    + endActivity.getEndTime());
        }
    }

}
