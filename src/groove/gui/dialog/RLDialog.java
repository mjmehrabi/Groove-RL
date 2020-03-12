
package groove.gui.dialog;


import groove.explore.Exploration;
import groove.grammar.QualName;
import groove.grammar.aspect.AspectEdge;


import groove.grammar.model.GrammarModel;
import groove.grammar.model.ResourceKind;
import groove.grammar.model.RuleModel;

import groove.gui.Icons;

import groove.gui.Simulator;

import groove.gui.layout.SpringUtilities;
import groove.util.parse.FormatException;
import groove.verify.*;


import java.awt.Color;
import java.awt.Cursor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.awt.event.KeyEvent;

import java.util.ArrayList;

import java.util.Iterator;

import java.util.Set;


import javax.swing.*;

/**
 *  @author Mohammad Javad Mehrabi
 *
 */


public class RLDialog extends JDialog  {

    private static final String START_COMMAND = "Start";
    private static final String Make_Knowlege_Base_COMMAND = "Make Knowlege Base";
    private static final String Enable_HostGraph_COMMAND = "Enable the Selected HostGraph";
    private static final String Explore_COMMAND = "Exploring & Learning & Model Checking";


    private static final String CANCEL_COMMAND = "Exit";

    private static final String START_TOOLTIP =
            "Restart with the customized exploration";

    private static final String Make_knowlege_base_TOOLTIP =
            "Make Knowlege Base...";


    private static final String Enable_HostGraph_TOOLTIP =
            "Enable The Selected HostGraph...";

    private static final String Explore_TOOLTIP =
            "Explore The State Space...";

    private static final String DEFAULT_TOOLTIP =
            "Set the currently selected exploration as the default for this grammar";


    /**
     * Color to be used for headers on the dialog.
     */
    public static final String HEADER_COLOR = "green";
    /**
     * Color to be used for text in the info panel.
     */
    public static final String INFO_COLOR = "#005050";
    /**
     * Color to be used for the background of the info panel.
     */
    public static final Color INFO_BG_COLOR = new Color(230, 230, 255);
    /**
     * Color to be used for the background boxes on the info panel.
     */
    public static final Color INFO_BOX_BG_COLOR = new Color(210, 210, 255);
    private JTextField txtMaxStateSize;
    private JTextField txtMaxActionOutput;
    private JTextField txtExperienceReplayMemorySize;
    private JTextField txtDiscountFactor;
    private JTextField txtEpsilonMin;
    private JTextField txtEpsilonDecay;
    private JTextField txtLearningRate;
    private JTextField txtBatchSize;
    private JTextField txtTargetModelUpdate;
    private JTextField txtHiddenLayerCount;
    private JTextField txtHiddenLayersNeuronSize;
    private JTextField txtEpisodes;
    private JTextField txtMaxStepPerEpisodeFrom;
    private JTextField txtMaxStepPerEpisodeTo;
    private JTextField txtMaxStepPerEpisodeIncrement;
    private JLabel rbDqn;
    private JRadioButton rbDoubledqn;
    private JRadioButton rbSimplememory;
    private JLabel rbPrioritizedmemory;
    private JRadioButton rbReward1;
    private JRadioButton rbReward2;
    private JCheckBox chkContinue;
    private JLabel lblResultsOfAllGoals;
    private JTextField txtResultsOfAllGoals;
    private JTextField txtTimeLimit;

    /**
     * Create the dialog.
     * @param simulator - reference to the simulator
     * @param owner - reference to the parent GUI component
     * @throws FormatException
     */
    public RLDialog(Simulator simulator, JFrame owner) {

        Alltype=new ArrayList<QualName>();

        this.simulator=simulator;
        //Create the content panel.
        JPanel dialogContent = new JPanel(new SpringLayout());

        // dialogContent.setBorder(BorderFactory.createEmptyBorder(10, 400, 400, 400));
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        dialogContent.registerKeyboardAction(createCloseListener(), escape,
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogContent.registerKeyboardAction(createCloseListener(), enter,
                JComponent.WHEN_IN_FOCUSED_WINDOW);



        JLabel jl=new JLabel("All Host Graphs");
        dialogContent.add(jl);

        cmbHostGraph= new JComboBox();
        cmbHostGraph.setBackground(INFO_BOX_BG_COLOR);
        dialogContent.add(cmbHostGraph);

        GrammarModel grammermodel=simulator.getModel().getGrammar();
        Set<QualName> sname= grammermodel.getNames(ResourceKind.HOST);
        Iterator<QualName> it=sname.iterator();
        while(it.hasNext())
        {
            QualName ts=it.next();
            cmbHostGraph.addItem(ts);
        }

        dialogContent.add(createEnableHostgraphPanel());

        JLabel line1=new JLabel("------------------------------------------------------------------------------------");
        line1.setForeground(Color.green);
        dialogContent.add(line1);

        dialogContent.add(createRBmodelcheckingPanel());

        JLabel line2=new JLabel("------------------------------------------------------------------------------------");
        line2.setForeground(Color.green);
        dialogContent.add(line2);



        dialogContent.add(createModelChecking());

        dialogContent.add(createRLPanel());

        dialogContent.add(createExplorePanel());



        JLabel line4=new JLabel("------------------------------------------------------------------------------------");
        line4.setForeground(Color.green);
        dialogContent.add(line4);

        JLabel jstep4=new JLabel("The Result of Model Checking");
        jstep4.setForeground(Color.blue);
        dialogContent.add(jstep4);

        txtresultOfmodelchecking=new JTextField("");
        txtresultOfmodelchecking.setForeground(INFO_BOX_BG_COLOR);
        txtresultOfmodelchecking.setEnabled(false);
        dialogContent.add(txtresultOfmodelchecking);

        JLabel jstep5=new JLabel("Time Spent (Seconds), The Number of explored states , The first found goal state depth ");
        jstep5.setForeground(Color.blue);
        dialogContent.add(jstep5);

        txtTimeSpent=new JTextField(" ");
        txtTimeSpent.setForeground(INFO_BOX_BG_COLOR);
        txtTimeSpent.setEnabled(false);
        dialogContent.add(txtTimeSpent);

        lblResultsOfAllGoals=new JLabel("Results of all found goal states: Goal Number, Time Spent (Seconds), The Number of explored states , The first found goal state depth");
        lblResultsOfAllGoals.setForeground(Color.blue);
        dialogContent.add(lblResultsOfAllGoals);
        txtResultsOfAllGoals=new JTextField("");
        dialogContent.add(txtResultsOfAllGoals);

        lblResultsOfAllGoals.setVisible(false);
        txtResultsOfAllGoals.setVisible(false);

        dialogContent.add(createCancelPanel());

        manageRB();

        SpringUtilities.makeCompactGrid(dialogContent, 17, 1, 5, 5, 15, 0);
        // Add the dialogContent to the dialog.
        add(dialogContent);
        setTitle("Model Checking (Reachability - Planning) by Reinforcement Learning ...");
        setIconImage(Icons.GROOVE_ICON_16x16.getImage());
        setSize(800, 800);  //width  height
        //pack();
        setLocationRelativeTo(owner);

//        setAlwaysOnTop(true);


        StartButton.setEnabled(true);

        setVisible(true);



    }
    /**
     * Creates the RL panel.
     */
    private JPanel createRLPanel() {
        JPanel rlPanel = new JPanel(new SpringLayout());
        rlPanel.setBackground(new Color(200, 200, 200));

//        rlPanel.add(new JLabel("Deep Q-Network(DQN) Type"));
        rbDoubledqn=new JRadioButton("Double Deep Q-Network");
//        rbDqn=new JRadioButton("Deep Q-Network");
        rbDqn=new JLabel("");
//        ButtonGroup option1 = new ButtonGroup();
        rbDqn.setBackground(new Color(200, 200, 200));
        rbDoubledqn.setBackground(new Color(200, 200, 200));
//        option1.add(rbDqn);
//        option1.add(rbDoubledqn);
        rlPanel.add(rbDoubledqn);
        rlPanel.add(rbDqn);
        rbDoubledqn.setSelected(true);

//        rlPanel.add(new JLabel("Experience Replay Memory Type"));
        rbSimplememory=new JRadioButton("Simple Experience Replay Memory");
//        rbPrioritizedmemory=new JRadioButton("Prioritized Experience Replay Memory");
        rbPrioritizedmemory=new JLabel("");
        rbSimplememory.setBackground(new Color(200, 200, 200));
        rbPrioritizedmemory.setBackground(new Color(200, 200, 200));
//        ButtonGroup option2 = new ButtonGroup();
//        option2.add(rbSimplememory);
//        option2.add(rbPrioritizedmemory);
        rlPanel.add(rbSimplememory);
        rlPanel.add(rbPrioritizedmemory);
        rbSimplememory.setSelected(true);

        //        rlPanel.add(new JLabel("Reward Type"));
        rbReward1=new JRadioButton("Reward 1 (General)");
        rbReward2=new JRadioButton("Reward 2 (Dedicated)");
        rbReward1.setBackground(new Color(200, 200, 200));
        rbReward2.setBackground(new Color(200, 200, 200));
        ButtonGroup option3 = new ButtonGroup();
        option3.add(rbReward1);
        option3.add(rbReward2);
        rlPanel.add(rbReward1);
        rlPanel.add(rbReward2);
        rbReward1.setSelected(true);

        rlPanel.add(new JLabel("Episodes"));
        txtEpisodes=new JTextField(10);
        txtEpisodes.setText("100");
        rlPanel.add(txtEpisodes);

        rlPanel.add(new JLabel("Steps"));
        txtMaxStepPerEpisodeFrom=new JTextField(5);
        txtMaxStepPerEpisodeFrom.setText("100");
        rlPanel.add(txtMaxStepPerEpisodeFrom);

//        rlPanel.add(new JLabel("Step Per Episode To"));
//        txtMaxStepPerEpisodeTo=new JTextField(5);
//        txtMaxStepPerEpisodeTo.setText("100");
//        rlPanel.add(txtMaxStepPerEpisodeTo);
//
//        rlPanel.add(new JLabel("Max Step Per Episode Increment"));
//        txtMaxStepPerEpisodeIncrement=new JTextField(10);
//        txtMaxStepPerEpisodeIncrement.setText("32");
//        rlPanel.add(txtMaxStepPerEpisodeIncrement);


//        rlPanel.add(new JLabel("Max State Size"));
//        txtMaxStateSize=new JTextField(10);
//        txtMaxStateSize.setText("800");
//        rlPanel.add(txtMaxStateSize);

        rlPanel.add(new JLabel("Max Action Output"));
        txtMaxActionOutput=new JTextField(10);
        txtMaxActionOutput.setText("400");
        rlPanel.add(txtMaxActionOutput);

        rlPanel.add(new JLabel("Experience Replay Memory Size"));
        txtExperienceReplayMemorySize=new JTextField(10);
        txtExperienceReplayMemorySize.setText("1000");
        rlPanel.add(txtExperienceReplayMemorySize);

//        rlPanel.add(new JLabel("Discount Factor - Between 0 & 1 (1 = Rewards In The Future Are Very Important)"));
        rlPanel.add(new JLabel("Discount Factor - Between 0 & 1"));
        txtDiscountFactor=new JTextField(10);
        txtDiscountFactor.setText("0.95");
        rlPanel.add(txtDiscountFactor);

        rlPanel.add(new JLabel("Minimum Value Of Epsilon"));
        txtEpsilonMin=new JTextField(10);
        txtEpsilonMin.setText("0.2");
        rlPanel.add(txtEpsilonMin);

        rlPanel.add(new JLabel("Decrease Value Of Epsilon in every step"));
        txtEpsilonDecay=new JTextField(10);
        txtEpsilonDecay.setText("0.995");
        rlPanel.add(txtEpsilonDecay);

        rlPanel.add(new JLabel("Learning Rate Of Neural Network (Suggestion: 0.1, 0.001, 0.00001)"));
        txtLearningRate=new JTextField(10);
        txtLearningRate.setText("0.001");
        rlPanel.add(txtLearningRate);

        rlPanel.add(new JLabel("Batch Size Of Training Dataset (Suggestion: 8, 16, 32, 64)"));
        txtBatchSize=new JTextField(10);
        txtBatchSize.setText("8");
        rlPanel.add(txtBatchSize);

//        rlPanel.add(new JLabel("Max Step Of Target Model Update"));
//        txtTargetModelUpdate=new JTextField(10);
//        txtTargetModelUpdate.setText("1");
//        rlPanel.add(txtTargetModelUpdate);

//        rlPanel.add(new JLabel("Hidden Layer Of Neural Network Count, > 0"));
//        txtHiddenLayerCount=new JTextField(10);
//        txtHiddenLayerCount.setText("1");
//        rlPanel.add(txtHiddenLayerCount);

//        rlPanel.add(new JLabel("Hidden Layer Neuron Size Of Each Layer (comma separated)- For Example 10,20 = 10 For #1 Hidden Layer, 20 For #2 Hidden Layer"));
//        rlPanel.add(new JLabel("Hidden Layer Neuron Size Of Each Layer (comma separated)"));
//        txtHiddenLayersNeuronSize=new JTextField(10);
//        txtHiddenLayersNeuronSize.setText("100");
//        rlPanel.add(txtHiddenLayersNeuronSize);

        rlPanel.add(new JLabel("Continue after finding a goal state"));
        chkContinue=new JCheckBox();
        rlPanel.add(chkContinue);

        JLabel jl=new JLabel("Time limit (sec): ");
        rlPanel.add(jl);
        txtTimeLimit=new JTextField("100");
        rlPanel.add(txtTimeLimit);

        chkContinue.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(chkContinue.isSelected()){
                    lblResultsOfAllGoals.setVisible(true);
                    txtResultsOfAllGoals.setVisible(true);
                }else{
                    lblResultsOfAllGoals.setVisible(false);
                    txtResultsOfAllGoals.setVisible(false);
                }
            }
        });

        SpringUtilities.makeCompactGrid(rlPanel, 14, 2, 5, 5, 15, 0);

        return rlPanel;
    }
      private JPanel createRBmodelcheckingPanel() {
        JPanel buttonPanel = new JPanel();

        rbreachability=new JRadioButton("Reachabiliy (EF q)");

        rbreachability.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                manageRB();
            }
        });

        buttonPanel.add(rbreachability);
        rbreachability.setSelected(true);
        ButtonGroup options = new ButtonGroup();
        options.add(rbreachability);
        return buttonPanel;
    }

    private JPanel createEnableHostgraphPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(getEnableHostGraphButton());
        return buttonPanel;
    }

    private JPanel createCancelPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(getCancelButton());
        return buttonPanel;
    }




    private JPanel createExplorePanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(getStartButton());
        return buttonPanel;
    }
    private void manageRB(){
        if(StartButton!=null){
            StartButton.setEnabled(true);

            cmbModelCheckingType.removeAllItems();
            cmbModelCheckingType.setEnabled(true);

            if(rbreachability.isSelected()){
                for(int i=0;i<=Alltype.size()-1;i++){
                    String s=Alltype.get(i).toString();
                    if(!s.contains("Live") && !s.contains("live")){
                        cmbModelCheckingType.addItem(s);
                    }

                }
            }

            if(cmbModelCheckingType.getItemCount()==0){
                StartButton.setEnabled(false);
            }
            else{
                StartButton.setEnabled(true);
            }


        }
    }

    private JPanel createModelChecking(){
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JLabel("The state property q:"));

        cmbModelCheckingType=new JComboBox();
        cmbModelCheckingType.setBackground(INFO_BOX_BG_COLOR);
        buttonPanel.add(cmbModelCheckingType);

        RulesCount=0;
        RulesName=new ArrayList<QualName>();

        GrammarModel grammermodel=simulator.getModel().getGrammar();
        Set<QualName> sname= grammermodel.getNames(ResourceKind.RULE);

        Iterator<QualName> it=sname.iterator();
        while(it.hasNext())
        {
            QualName ts=it.next();
            RuleModel rulemodel=grammermodel.getRuleModel(ts);
            if(rulemodel.isEnabled()){
                Set<? extends AspectEdge> edgeSet=rulemodel.getSource().edgeSet();

                boolean flag=false;
                for(AspectEdge ae:edgeSet ){
                    //if(ae.toString().contains("new:") ||ae.toString().contains("del:") || ae.toString().contains("not:") ){
                    if(ae.toString().contains("new:") ||ae.toString().contains("del:") ){
                        flag=true;
                        break;
                    }
                }
                if(!flag){
                    try{
                        if(rulemodel.toResource().getAnchor().size()>0)
                            flag=true;
                    }
                    catch (FormatException e) {

                        e.printStackTrace();
                    }
                }

                if(!flag)
                    cmbModelCheckingType.addItem(ts);
                else{
                    RulesCount++;
                    RulesName.add(ts);

                }
            }
        }
        return buttonPanel;
    }

    /**
     * Creates the Genetic panel.
     */


    private int RulesCount;
    public ArrayList<QualName> RulesName;


    private RefreshButton cancelButton;


    private RefreshButton StartButton;
    private RefreshButton EnableHostGraphButton;


    ArrayList<QualName> Alltype;


    private Simulator simulator;
    private RL rl;




    private JTextField txtresultOfmodelchecking;
    private JTextField txtTimeSpent;
    private JComboBox cmbModelCheckingType;
    private JTextField txtMaxNumberOfStates;
    private  JComboBox cmbHostGraph;



    private  JRadioButton rbreachability;

    private RefreshButton getEnableHostGraphButton() {
        if (this.EnableHostGraphButton == null) {
            // Create the explore button (reference is needed when setting the
            // initial value of the (strategy/acceptor) editors.
            this.EnableHostGraphButton = new RefreshButton(Enable_HostGraph_COMMAND) {
                @Override
                public void execute() {

                    //if(heuristicreach==null)
                    rl=new RL();



                    Alltype=new ArrayList<QualName>();

                    GrammarModel grammermodel=simulator.getModel().getGrammar();
                    Set<QualName> sname= grammermodel.getNames(ResourceKind.RULE);

                    Iterator<QualName> it=sname.iterator();
                    while(it.hasNext()){
                        QualName ts=it.next();
                        RuleModel rulemodel=grammermodel.getRuleModel(ts);
                        if(rulemodel.isEnabled()){
                            Set<? extends AspectEdge> edgeSet=rulemodel.getSource().edgeSet();

                            boolean flag=false;
                            for(AspectEdge ae:edgeSet ){
                                //if(ae.toString().contains("new:") ||ae.toString().contains("del:") || ae.toString().contains("not:") ){
                                if(ae.toString().contains("new:") ||ae.toString().contains("del:")  ){
                                    flag=true;
                                    break;
                                }
                            }
            	        		/*
            	        		if(!flag){
            	        			try{
            	        				if(rulemodel.toResource().getAnchor().size()>0)
            	        					flag=true;
            	        			}
            	        			catch (FormatException e) {
            	        				// TODO Auto-generated catch block
            	        				e.printStackTrace();
            	        			}
            	        		}
            	        		*/

                            if(!flag)
                                Alltype.add(ts);
                        }
                    }


                    FillModelCheckingType(Alltype);




                    String HostGraphName;
                    HostGraphName=cmbHostGraph.getSelectedItem().toString();
                    rl.simulator=simulator;
                    rl.HostGraphName=HostGraphName;
                    rl.EnableSelectedHostGraph();

                    StartButton.setEnabled(true);

                    manageRB();
                }

                @Override
                public void refresh(Exploration exploration) {
                    setEnabled(Enable_HostGraph_TOOLTIP, exploration);
                }
            };
        }
        return this.EnableHostGraphButton;
    }
    public void FillModelCheckingType(ArrayList<QualName> alltype){
        //cmbModelCheckingType.removeAll();
        cmbModelCheckingType.removeAllItems();
        for(int i=0;i<=alltype.size()-1;i++){
            QualName s=alltype.get(i);
            cmbModelCheckingType.addItem(s);
            cmbModelCheckingType.setSelectedIndex(0);
        }
    }

    private long startUsedMemory;

    private RefreshButton getStartButton() {
        if (this.StartButton== null) {
            // Create the explore button (reference is needed when setting the
            // initial value of the (strategy/acceptor) editors.
            this.StartButton = new RefreshButton(Explore_COMMAND) {
                @Override
                public void execute() {

                    txtresultOfmodelchecking.setText("");
                    txtTimeSpent.setText("");


                    if(rl==null)
                        rl=new RL();

                    String modelcheckingType =cmbModelCheckingType.getSelectedItem().toString();




                    if(rbreachability.isSelected())
                        rl.CTLproperty ="reachability";

                    rl.ModelCheckingTarget=modelcheckingType;
                    rl.episodes = Integer.parseInt(txtEpisodes.getText());
                    rl.fromMaxStep = Integer.parseInt(txtMaxStepPerEpisodeFrom.getText());
                    rl.toMaxStep = Integer.parseInt(txtMaxStepPerEpisodeFrom.getText());
//                    rl.toMaxStep = Integer.parseInt(txtMaxStepPerEpisodeTo.getText());
                    rl.maxStepIncrement = 1;
//                    rl.maxStepIncrement = Integer.parseInt(txtMaxStepPerEpisodeIncrement.getText());
                    rl.batchSize = Integer.parseInt(txtBatchSize.getText());
//                    rl.maxStateSize = Integer.parseInt(txtMaxStateSize.getText());
                    rl.maxStateSize = rl.toMaxStep + 1;
                    rl.maxActionOutput = Integer.parseInt(txtMaxActionOutput.getText());
//                    rl.maxActionOutput = 400;
                    rl.experienceReplayMemorySize = Integer.parseInt(txtExperienceReplayMemorySize.getText());
                    rl.discountFactor = Float.parseFloat(txtDiscountFactor.getText());
                    rl.epsilonMin = Float.parseFloat(txtEpsilonMin.getText());
                    rl.epsilonDecay = Float.parseFloat(txtEpsilonDecay.getText());
                    rl.learningRate = Float.parseFloat(txtLearningRate.getText());
//                    rl.targetModelUpdateStep = Integer.parseInt(txtTargetModelUpdate.getText());
                    rl.targetModelUpdateStep = 1;
//                    rl.hiddenLayerCount = Integer.parseInt(txtHiddenLayerCount.getText());
                    rl.hiddenLayerCount = 1;
                    rl.dqnAgentType = (rbDoubledqn.isSelected() ? 2 : 1);
                    rl.memoryType = !rbSimplememory.isSelected();
                    rl.findTheBestGoal = chkContinue.isSelected();
                    rl.timeLimit=Integer.parseInt(txtTimeLimit.getText().trim());
//                    String[] nsize = txtHiddenLayersNeuronSize.getText().split(",");
//                    rl.hiddenLayersNeuronSize = new int[nsize.length];
//                    for (int i = 0; i<nsize.length; i++) {
//                        rl.hiddenLayersNeuronSize[i] = Integer.parseInt(nsize[i].trim());
//                    }
                    rl.hiddenLayersNeuronSize = new int[]{rl.maxStateSize * 2/3, rl.maxStateSize * 2/3, rl.maxStateSize * 2/3, rl.maxStateSize * 2/3, rl.maxStateSize * 2/3};
                    if (rbReward1.isSelected())
                        rl.rewardType = 1;
                    else
                        rl.rewardType = 2;
                    if (rl.dqnAgentType == RL.DEEP_Q_NETWORK_AGENT)
                        rl.dqnAgent = new DQNAgent(rl.maxStateSize, rl.maxActionOutput, rl.experienceReplayMemorySize, rl.discountFactor, rl.epsilonMin, rl.epsilonDecay, rl.learningRate, rl.hiddenLayerCount, rl.hiddenLayersNeuronSize, rl.memoryType, rl.batchSize);
                    else
                        rl.dqnAgent = new DDQNAgent(rl.maxStateSize, rl.maxActionOutput, rl.experienceReplayMemorySize, rl.discountFactor, rl.epsilonMin, rl.epsilonDecay, rl.learningRate, rl.hiddenLayerCount, rl.hiddenLayersNeuronSize, rl.memoryType, rl.batchSize);

                    rl.Alltype=Alltype;

                    setCursor(new Cursor(Cursor.WAIT_CURSOR));

                    rl.simulator=simulator;

                    long startTime = System.currentTimeMillis();
                    rl.lastTime = startTime + rl.timeLimit * 1000;

                    //final Runtime runTime = Runtime.getRuntime();
                    //startUsedMemory = runTime.totalMemory() - runTime.freeMemory();

                    String s=rl.Explore(modelcheckingType,RulesCount,RulesName,null,null);

                    //long usedMemory = runTime.totalMemory() - runTime.freeMemory();
                    //long RealusedMemory=(usedMemory - startUsedMemory)/1024;




                    String heuristicResult=rl.exploringItems.heuristicResult;
                    String lastStateInReachability="";
                    int i=heuristicResult.indexOf("_");
                    if(i>=0){
                        lastStateInReachability=heuristicResult.substring(i+1);
                        heuristicResult=heuristicResult.substring(0,i);
                    }

                    boolean flag=false;
                    if(heuristicResult==null)
                        heuristicResult="noreachability";

                    if(heuristicResult.equals("reachability"))
                        flag=true;
                    else
                        flag=false;


                    if(rbreachability.isSelected()){
                        if(heuristicResult.equals("reachability"))
                            heuristicResult="The propery is verified."+"..Last state is: "+lastStateInReachability;
                        else
                            heuristicResult="The propery is not verified.";
                    }


                    txtresultOfmodelchecking.setText(heuristicResult);
                    long reportTime= System.currentTimeMillis() - startTime;
                    String spenttime= String.valueOf(reportTime/1000)+" second :"+ String.valueOf(reportTime % 1000) +" millisecond ";



                    String S1=String.valueOf(reportTime/1000.0);
                    String S2=String.valueOf(rl.Number_Explored_States);
                    String S3=String.valueOf(rl.First_Found_Dead_depth);


                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                    if(flag && !rl.findTheBestGoal)
                        txtTimeSpent.setText(S1+" , " + S2 +" , " +S3 );
                    else
                        txtTimeSpent.setText("");

                    if(rl.findTheBestGoal){
                        String multipleS="";
                        for(i=0;i<rl.exploringItems.goalStatesInfo.size();i++){
                            ExploringItemRL.GoalState goalstate=rl.exploringItems.goalStatesInfo.get(i);
                            multipleS+="  GoalState ("+(i+1)+"): "+String.valueOf(goalstate.foundTime/1000.0)+", "+ String.valueOf(goalstate.exploredstate) + ", " + String.valueOf(goalstate.witnessLength);
                        }
                        txtResultsOfAllGoals.setText(multipleS);
                    }


                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    reportTime=0;

                    if(rl==null)
                        rl=new RL();
                    /////////////////////////////////////

                }

                @Override
                public void refresh(Exploration exploration) {
                    setEnabled(Explore_TOOLTIP, exploration);
                }
            };
        }
        return this.StartButton;
    }


    /** Initialises and returns the cancel button. */
    private RefreshButton getCancelButton() {
        if (this.cancelButton == null) {
            // Create the explore button (reference is needed when setting the
            // initial value of the (strategy/acceptor) editors.
            this.cancelButton = new RefreshButton(CANCEL_COMMAND) {
                @Override
                public void execute() {
                    closeDialog();
                }

                @Override
                public void refresh(Exploration exploration) {
                    // do nothing
                }
            };
        }
        return this.cancelButton;
    }


    /**
     * Action that responds to Escape. Ensures that the dialog is closed.
     */
    private ActionListener createCloseListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                closeDialog();

            }
        };
    }
    /**
     * The close dialog action. Disposes dialog and resets DismissDelay of the
     * ToolTipManager.
     */
    private void closeDialog() {
        this.dispose();
    }

    private abstract class RefreshButton extends JButton {
        /** Constructs a refreshable button with a given button text. */
        public RefreshButton(String text) {
            super(text);
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    execute();
                }
            });
        }

        /** Callback action invoked on button click. */
        public abstract void execute();

        /** Callback action allowing the button to refresh its status. */
        public abstract void refresh(Exploration exploration);


        protected void setEnabled(String toolTipText, Exploration exploration) {
            setEnabled(true);
        }
    }

}
