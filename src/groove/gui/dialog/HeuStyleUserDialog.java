
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


import groove.verify.HeuStyleUser;

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



import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;


/**
 *  @author Einollah Pira & Vahid Rafe
 *  
 */



public class HeuStyleUserDialog extends JDialog  {

	private static final String START_COMMAND = "Start";
	private static final String Make_Knowlege_Base_COMMAND = "Make Knowlege Base & Data Mining";
	private static final String Enable_HostGraph_COMMAND = "Enable the Selected HostGraph";
	private static final String Explore_COMMAND = "Explore State Space";
	
	
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

    /**
     * Create the dialog.
     * @param simulator - reference to the simulator
     * @param owner - reference to the parent GUI component
     * @throws FormatException
     */
    public HeuStyleUserDialog(Simulator simulator, JFrame owner) {
    	
    	AllreportTime=0;
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
        
        
        ModelCheckingType="Rechability";
        
        
        
        
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
	        		/*if(!flag){
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

     	
        
        JLabel jstep1=new JLabel("Step1(Learning)");
        jstep1.setForeground(Color.red);
        dialogContent.add(jstep1);
        
        
        
        JLabel jl=new JLabel("All Host Graphs (select a small model)");
        dialogContent.add(jl);
        
        cmbHostGraph= new JComboBox();
        cmbHostGraph.setBackground(INFO_BOX_BG_COLOR);
        dialogContent.add(cmbHostGraph);
        
         grammermodel=simulator.getModel().getGrammar();
        sname= grammermodel.getNames(ResourceKind.HOST);
       	 it=sname.iterator();
       	while(it.hasNext())
       	{
       		QualName ts=it.next();
       		cmbHostGraph.addItem(ts);
       	}
     
        
        
       
       
        
       	dialogContent.add(createEnableHostgraphPanel());
       	dialogContent.add(createExplorePanel());
       	
       	
         	
       	dialogContent.add(createRBmodelcheckingPanel());
    	dialogContent.add(createRBmodelcheckingLivenessPanel());
       	
       	
   
       	
       
      	 JLabel jstep2=new JLabel("Step2(Make a Knowlege Base & Data Mining)");
         jstep2.setForeground(Color.red);
         dialogContent.add(jstep2);
    	
    	
    	dialogContent.add(new JLabel("The state property q:"));
        
    	cmbModelCheckingType=new JComboBox();
    	cmbModelCheckingType.setBackground(INFO_BOX_BG_COLOR);
        dialogContent.add(cmbModelCheckingType);
        
        
        dialogContent.add(createRBDataMiningPanel());
        
        
        dialogContent.add(createKnowlegeBasePanel());
         
        JLabel jstep3=new JLabel("Step3(Model Checking)");
        jstep3.setForeground(Color.red);
        dialogContent.add(jstep3);
        
        
        JLabel jl2=new JLabel("All Host Graphs (select a large model)");
        dialogContent.add(jl2);
        
        cmbHostGraph2= new JComboBox();
        cmbHostGraph2.setBackground(INFO_BOX_BG_COLOR);
        dialogContent.add(cmbHostGraph2);
        
        GrammarModel grammermodel2=simulator.getModel().getGrammar();
        Set<QualName> sname2= grammermodel.getNames(ResourceKind.HOST);
       	Iterator<QualName> it2=sname.iterator();
       	while(it2.hasNext())
       	{
       		QualName ts=it2.next();
       		cmbHostGraph2.addItem(ts);
       	}
       	
    	dialogContent.add(createEnableHostgraphPanel2());
       	
    	
    	
        
    	dialogContent.add(new JLabel("The state property q:"));
        
    	cmbModelCheckingTypeT=new JComboBox();
    	cmbModelCheckingTypeT.setBackground(INFO_BOX_BG_COLOR);
        dialogContent.add(cmbModelCheckingTypeT);
        
    	
        dialogContent.add(createStartPanel());
        
        manageRB();
        
        JLabel jstep4=new JLabel("The Result of Model Checking");
        jstep4.setForeground(Color.blue);
        dialogContent.add(jstep4);
        
        txtresultOfmodelchecking=new JTextField("");
        txtresultOfmodelchecking.setForeground(INFO_BOX_BG_COLOR);
        txtresultOfmodelchecking.setEnabled(false);
        dialogContent.add(txtresultOfmodelchecking);
        
        JLabel jstep5=new JLabel("Total time spent (Exhaustive exploring of the samller model + Executing of the FindFreqPatt algorithm");
        JLabel jstep6=new JLabel("   +Efficient exploring of the large model+...) , Number of explored states");
        jstep5.setForeground(Color.blue);
        jstep6.setForeground(Color.blue);
        dialogContent.add(jstep5);
        dialogContent.add(jstep6);
        
        
        txtTimeSpent=new JTextField(" ");
        txtTimeSpent.setForeground(Color.red);
        txtTimeSpent.setEnabled(false);
        dialogContent.add(txtTimeSpent);
        
        dialogContent.add(createCancelPanel());
        
        
        SpringUtilities.makeCompactGrid(dialogContent, 25, 1, 5, 5, 15, 0);
        // Add the dialogContent to the dialog.
        add(dialogContent);
        setTitle("Model Checking by Data Mining (from exploring of the minimized problem) ...");
        setIconImage(Icons.GROOVE_ICON_16x16.getImage());
        setSize(770, 740);  //width height
        //pack();
        setLocationRelativeTo(owner);
        
        /////
        EnableHostGraphButton.setEnabled(true);
        ExploreButton.setEnabled(false);
  
        MakeKnowlegeBaseButton.setEnabled(false);
        EnableHostGraphButton2.setEnabled(false);
        startButton.setEnabled(false);
        ////
        
        setVisible(true);
        
     }
    private JPanel createRBDataMiningPanel() {
    	JPanel DMPanel = new JPanel();
    	rbapriori=new JRadioButton("Apriori");
    	rbfpgrowth=new JRadioButton("FPGrowth");
    	rbeclat=new JRadioButton("Eclat");
    	rbfin=new JRadioButton("Fin");
    	
    	
    	rbapriori.setSelected(true);
    	DMPanel.add(rbapriori);
    	DMPanel.add(rbfpgrowth);
    	DMPanel.add(rbeclat);
    	DMPanel.add(rbfin);
    	
    	
    	
    	ButtonGroup options = new ButtonGroup();
    	options.add(rbapriori);
    	options.add(rbfpgrowth);
    	options.add(rbeclat);
    	options.add(rbfin);
    	
        return DMPanel;
    }
  
    private JPanel createStartPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(getStartButton());
        return buttonPanel;
    }
    
    
    private JPanel createCancelPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(getCancelButton());
        return buttonPanel;
    }

   
    private JPanel createKnowlegeBasePanel() {
    	JPanel buttonPanel = new JPanel();
    	buttonPanel.add(getMakeKnowlegeBaseButton());
        return buttonPanel;
    }
    
    private JPanel createEnableHostgraphPanel() {
    	JPanel buttonPanel = new JPanel();
    	buttonPanel.add(getEnableHostGraphButton());
        return buttonPanel;
    }
    
    private JPanel createEnableHostgraphPanel2() {
    	JPanel buttonPanel = new JPanel();
    	buttonPanel.add(getEnableHostGraphButton2());
        return buttonPanel;
    }
    private JPanel createExplorePanel() {
    	JPanel buttonPanel = new JPanel();
    	buttonPanel.add(getExploreButton());
        return buttonPanel;
    }
 
    private JPanel createRBmodelcheckingPanel() {
    	JPanel buttonPanel = new JPanel();
    	rbdeadlock=new JRadioButton("Deadlock (EF q)");
    	rbreachability=new JRadioButton("Reachabiliy (EF q)");
    	rbsafety=new JRadioButton("Refutation of Safety (AG !q)");
    	
    	rbdeadlock.addItemListener(new ItemListener() {
             public void itemStateChanged(ItemEvent e) {             
            	 manageRB();
             }           
          });
    	
    	rbreachability.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {             
           	 manageRB();
            }           
         });
    	
    	rbsafety.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {             
           	 manageRB();
            }           
         });
    	
    	rbdeadlock.setSelected(true);
    	//rbreachability.setEnabled(false);
    	
    	buttonPanel.add(rbdeadlock);
    	buttonPanel.add(rbreachability);
    	buttonPanel.add(rbsafety);
    	
        return buttonPanel;
    }
    
    
    private JPanel createRBmodelcheckingLivenessPanel() {
    	JPanel buttonPanel = new JPanel();
    	
       	rbLivenessDead=new JRadioButton("Refutation of liveness by a path leading to a deadlock");
    	rbLivenessCycle=new JRadioButton("Refutation of liveness by a path leading to a cycle");
    	
    	
    	buttonPanel.add(rbLivenessDead);
    	buttonPanel.add(rbLivenessCycle);
    
    	rbLivenessDead.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {             
           	 manageRB();
            }           
         });
    	rbLivenessCycle.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {             
           	 manageRB();
            }           
         });
    	
    	ButtonGroup options = new ButtonGroup();
    	options.add(rbdeadlock);
    	options.add(rbreachability);
    	options.add(rbsafety);
    	options.add(rbLivenessDead);
    	options.add(rbLivenessCycle);
    	
    	
        return buttonPanel;
    }
    
    
    

    
    private RefreshButton startButton;
    private RefreshButton cancelButton;
    private RefreshButton MakeKnowlegeBaseButton;
    private RefreshButton EnableHostGraphButton;
    private RefreshButton ExploreButton;
    private RefreshButton EnableHostGraphButton2;
    
    
    
    
    
    private JTextField  txtPopulation;
    private JTextField txtIterations;
    private JTextField txtDepthOfSearch;
    private JTextField txtMutationRate;
    private JTextField txtCrossOverRate;
    private Simulator simulator;
    private HeuStyleUser heuristicreach;
    private JComboBox cmbModelCheckingType;
    private JComboBox cmbModelCheckingTypeT;
    private  JComboBox cmbHostGraph;
    private  JComboBox cmbHostGraph2;
  
    private  JRadioButton rbdeadlock;
    private  JRadioButton rbreachability;
    private  JRadioButton rbsafety;
    private  JRadioButton rbLivenessDead;
    private  JRadioButton rbLivenessCycle;
    
    private  JRadioButton rbapriori;
    private  JRadioButton rbfpgrowth;
    private  JRadioButton rbeclat;

    private  JRadioButton rbfin;
    
    private JTextField txtresultOfmodelchecking;
    private JTextField txtTimeSpent;
    ArrayList<QualName> Alltype;
    
    private String ModelCheckingType;   //DeadLock   Reachability  RefuteSafety RefuteLivenessByDead RefuteLivenessByCycle
    private String ModelCheckingTarget;   //property name
    private String DataMiningType;  
    
    
    
    
    /** Initialises and returns the start button. */
    private RefreshButton getStartButton() {
        if (this.startButton == null) {
            // Create the explore button (reference is needed when setting the
            // initial value of the (strategy/acceptor) editors.
            this.startButton = new RefreshButton(START_COMMAND) {
                @Override
                public void execute() {
                	  
                	if(heuristicreach==null)
                		heuristicreach=new HeuStyleUser();
                	
                	
                	/////
                    EnableHostGraphButton.setEnabled(true);
                    ExploreButton.setEnabled(false);
              
                    MakeKnowlegeBaseButton.setEnabled(false);
                    EnableHostGraphButton2.setEnabled(true);
                    startButton.setEnabled(true);
                    ////
                	
               	 	
            	 	ModelCheckingType="";
               	 	ModelCheckingTarget="";
               	 	   
	               	if(rbreachability.isSelected() && cmbModelCheckingTypeT.getSelectedIndex()>=0 ){
	         	    	ModelCheckingType="Reachability";
	         	    	ModelCheckingTarget=cmbModelCheckingTypeT.getSelectedItem().toString();
	           	 	}
	               	
	               	if(rbdeadlock.isSelected() && cmbModelCheckingTypeT.getSelectedIndex()>=0 ){
	         	    	ModelCheckingType="DeadLock";
	         	    	ModelCheckingTarget="DeadLock";
	           	 	}
	               	
               	   
               	 	if(rbLivenessDead.isSelected()){
            	    	ModelCheckingType="DeadLock";
            	    	ModelCheckingTarget="DeadLock";
            	    }
               	 	if(rbLivenessCycle.isSelected() && cmbModelCheckingTypeT.getSelectedIndex()>=0 ){
	         	    	ModelCheckingType="RefuteLivenessByCycle";
	         	    	ModelCheckingTarget=cmbModelCheckingTypeT.getSelectedItem().toString();
               	 	}
               	 	if(rbsafety.isSelected() ){
	         	    	ModelCheckingType="DeadLock";
	         	    	ModelCheckingTarget="DeadLock";
            	 	}
               	 	
            	    
               	 	heuristicreach.simulator=simulator;
               	 	heuristicreach.ModelCheckingType=ModelCheckingType;
               	 	heuristicreach.ModelCheckingTarget=ModelCheckingTarget;
            	 	
                	
               		setCursor(new Cursor(Cursor.WAIT_CURSOR));
               	 	
               	 	txtresultOfmodelchecking.setText("");
               	 	txtTimeSpent.setText("");
               	 	
                	long startTime = System.currentTimeMillis();
               	    String heuristicResult=heuristicreach.start();
               	    if(heuristicResult==null)
               	    	heuristicResult="noreachability";
               	    
               	    
               	 if(rbdeadlock.isSelected()){
              		  if(heuristicResult.equals("reachability"))
                    	   	heuristicResult="The propery is verified.";
                    	else
                    	   	heuristicResult="The propery is not verified.";
              	   }
              	   if(rbreachability.isSelected()){
            		  if(heuristicResult.equals("reachability"))
                  	   	heuristicResult="The propery is verified.";
                  	 else
                  	   	heuristicResult="The propery is not verified.";
            	   }
	               	if(rbsafety.isSelected() ){
	           		  if(heuristicResult.equals("reachability"))
	                 	   	heuristicResult="The propery is refuted.";
	                 	 else
	                 	   	heuristicResult="The propery is not refuted.";
	           	   }
	               	if(rbLivenessDead.isSelected() ){
		           		  if(heuristicResult.equals("reachability"))
		                 	   	heuristicResult="The propery is refuted.";
		                 	 else
		                 	   	heuristicResult="The propery is not refuted.";
		           	 }

	               	if(rbLivenessCycle.isSelected() ){
		           		  if(heuristicResult.equals("reachability"))
		                 	   	heuristicResult="The propery is refuted.";
		                 	 else
		                 	   	heuristicResult="The propery is not refuted.";
		           	 }

	               	
               	    
               	    txtresultOfmodelchecking.setText(heuristicResult);
               	    long reportTime= System.currentTimeMillis() - startTime;
               	    reportTime_Effi_Explo_Large=reportTime;
               	    
               	    AllreportTime=reportTime_Exhau_Explo_Small+reportTime_Effi_Explo_Large+reportTime_EnableHostGraph1+reportTime_EnableHostGraph2+reportTime_Data_Mining;
               	    
               	    String S1=String.valueOf(AllreportTime/1000.0);
               	    String S2=String.valueOf(reportTime_Exhau_Explo_Small/1000.0);
               	    String S3=String.valueOf((double)reportTime_Data_Mining/1000.0);
               	    String S4=String.valueOf(reportTime_Effi_Explo_Large/1000.0);
               	    String S5=String.valueOf(heuristicreach.Number_Explored_States);
               	                  	    
               	    
               	    txtTimeSpent.setText(S1+" ( "+S2+" + "+S3+" + "+S4+" + ..." +" ) "+" , "+S5);
               	    
               	    
               	    ///JLabel jstep5=new JLabel("Total time spent (Exhaustive exploring of the samller model + Executing of the FindFreqPatt algorithm");
                    //JLabel jstep6=new JLabel("   +Efficient exploring of the large model),Number of explored states");
               	    
               	    AllreportTime=0;
               	    
               		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                	                	
                }

                @Override
                public void refresh(Exploration exploration) {
                    setEnabled(START_TOOLTIP, exploration);
                }
            };
        }
        return this.startButton;
    }
    private void manageRB(){
   	
    	
    	/////
    	if(startButton!=null){
        	
        	
        	cmbModelCheckingType.removeAllItems();
           	cmbModelCheckingType.setEnabled(true);
    	
           	cmbModelCheckingTypeT.removeAllItems();
           	cmbModelCheckingTypeT.setEnabled(true);
           	
        	if(rbdeadlock.isSelected() || rbsafety.isSelected()){
        		cmbModelCheckingType.addItem("DeadLock");
        		cmbModelCheckingType.setEnabled(false);
        		cmbModelCheckingTypeT.addItem("DeadLock");
        		cmbModelCheckingTypeT.setEnabled(false);

        	}
        	if(rbreachability.isSelected()){
        		for(int i=0;i<=Alltype.size()-1;i++){
        			String s=Alltype.get(i).toString();
        			if(!s.contains("DeadLock") && !s.contains("Live") && !s.contains("live")){
        				cmbModelCheckingType.addItem(s);
        				cmbModelCheckingTypeT.addItem(s);
        			}
        				
        		}
        	}
        	
        	if(rbLivenessDead.isSelected() || rbLivenessCycle.isSelected()){
        		for(int i=0;i<=Alltype.size()-1;i++){
        			String s=Alltype.get(i).toString();
        			if(s.contains("Live") || s.contains("live")){
        				cmbModelCheckingType.addItem(s);
        				cmbModelCheckingTypeT.addItem(s);
        			}
        				
        		}
        	}
        	
        	if(cmbModelCheckingType.getItemCount()==0){
        		    EnableHostGraphButton.setEnabled(true);
        	        ExploreButton.setEnabled(false);
        	        MakeKnowlegeBaseButton.setEnabled(false);
        	        EnableHostGraphButton2.setEnabled(false);
        	        startButton.setEnabled(false);
        	}
        	else{
        		MakeKnowlegeBaseButton.setEnabled(true);
        	}
        	
    	}
    
    
    }
    
    /** Initialises and returns the start button. */
    private RefreshButton getMakeKnowlegeBaseButton() {
        if (this.MakeKnowlegeBaseButton == null) {
            this.MakeKnowlegeBaseButton = new RefreshButton(Make_Knowlege_Base_COMMAND) {
                @Override
                public void execute() {
                	  
                	if(heuristicreach==null)
                		heuristicreach=new HeuStyleUser();
                	
                	
                	/////
                    EnableHostGraphButton.setEnabled(true);
                   
                    
                    MakeKnowlegeBaseButton.setEnabled(true);
                    EnableHostGraphButton2.setEnabled(true);
                    
                    ////
              
                	ModelCheckingType="";
               	 	ModelCheckingTarget="";
               	 	   
	               	if(rbreachability.isSelected() && cmbModelCheckingType.getSelectedIndex()>=0 ){
	         	    	ModelCheckingType="Reachability";
	         	    	ModelCheckingTarget=cmbModelCheckingType.getSelectedItem().toString();
	           	 	}
	               	
	               	if(rbdeadlock.isSelected() && cmbModelCheckingType.getSelectedIndex()>=0 ){
	         	    	ModelCheckingType="DeadLock";
	         	    	ModelCheckingTarget="DeadLock";
	           	 	}
	               	
               	   
               	 	if(rbLivenessDead.isSelected()){
            	    	ModelCheckingType="DeadLock";
            	    	ModelCheckingTarget="DeadLock";
            	    }
               	 	if(rbLivenessCycle.isSelected() && cmbModelCheckingType.getSelectedIndex()>=0 ){
	         	    	ModelCheckingType="RefuteLivenessByCycle";
	         	    	ModelCheckingTarget=cmbModelCheckingType.getSelectedItem().toString();
               	 	}
               	 	if(rbsafety.isSelected() ){
	         	    	ModelCheckingType="DeadLock";
	         	    	ModelCheckingTarget="DeadLock";
            	 	}
               	 	            	    	
               	    
               	    
                    if(rbapriori.isSelected() ){
	         	    	DataMiningType="Apriori";
	         	 	}
                    if(rbfpgrowth.isSelected()){
                    	DataMiningType="FpGrowth";
                    }
                    if(rbeclat.isSelected()){
                    	DataMiningType="Eclat";
                    }
                    
                    if(rbfin.isSelected()){
                    	DataMiningType="Fin";
                    }
               	 	heuristicreach.simulator=simulator;
               	 	heuristicreach.ModelCheckingTarget=ModelCheckingTarget;
               	                    	
               	 	long startTime = System.currentTimeMillis();
            	    
                	heuristicreach.ModelCheckingType=ModelCheckingType;
                	heuristicreach.DataMiningType=DataMiningType;
               	 	heuristicreach.simulator=simulator;
               	 	heuristicreach.MakeKnowlegeBase();
               	 	if(heuristicreach.ALearningItems.size()==0)
               	 		startButton.setEnabled(false);
               	 	else
               	 		startButton.setEnabled(true);
               	 	
               	   reportTime_Data_Mining= System.currentTimeMillis() - startTime;
               	 	
               	   String s1=String.valueOf((double)reportTime_Data_Mining/1000.0);
               	   MakeKnowlegeBaseButton.setText(Make_Knowlege_Base_COMMAND +" : "+s1);

                }

                @Override
                public void refresh(Exploration exploration) {
                    setEnabled(Make_knowlege_base_TOOLTIP, exploration);
                }
            };
        }
        return this.MakeKnowlegeBaseButton;
    }
    
    private RefreshButton getEnableHostGraphButton() {
        if (this.EnableHostGraphButton == null) {
            // Create the explore button (reference is needed when setting the
            // initial value of the (strategy/acceptor) editors.
            this.EnableHostGraphButton = new RefreshButton(Enable_HostGraph_COMMAND) {
                @Override
                public void execute() {
                	  
                	if(heuristicreach==null)
                		heuristicreach=new HeuStyleUser();
                	
               	 	
               	 	
                	long startTime = System.currentTimeMillis();
            	    
               	 	
                	/////
                    EnableHostGraphButton.setEnabled(true);
                    ExploreButton.setEnabled(true);
                    ////
               	 	
               	 	
                	
               	 	
               	    String HostGraphName;
               	    HostGraphName=cmbHostGraph.getSelectedItem().toString();
            	 	heuristicreach.simulator=simulator;
            	 	heuristicreach.HostGraphName=HostGraphName;
            	 	heuristicreach.EnableSelectedHostGraph();
               	 	
            	 	reportTime_EnableHostGraph1= System.currentTimeMillis() - startTime;
               	 	
            	 	
            	 	
               	 	
                }

                @Override
                public void refresh(Exploration exploration) {
                    setEnabled(Enable_HostGraph_TOOLTIP, exploration);
                }
            };
        }
        return this.EnableHostGraphButton;
    }
    private RefreshButton getEnableHostGraphButton2() {
        if (this.EnableHostGraphButton2 == null) {
            // Create the explore button (reference is needed when setting the
            // initial value of the (strategy/acceptor) editors.
            this.EnableHostGraphButton2 = new RefreshButton(Enable_HostGraph_COMMAND) {
                @Override
                public void execute() {
                	  
                	if(heuristicreach==null)
                		heuristicreach=new HeuStyleUser();
                	
                	long startTime = System.currentTimeMillis();
            	    
            	 	
                	/////
                    EnableHostGraphButton.setEnabled(false);
                    ExploreButton.setEnabled(false);
                   
                    MakeKnowlegeBaseButton.setEnabled(false);
                    EnableHostGraphButton2.setEnabled(true);
                    startButton.setEnabled(true);
                    ////
               	 	
            	 	
                	
                	
                	String HostGraphName;
                 	HostGraphName=cmbHostGraph2.getSelectedItem().toString();
              	 	heuristicreach.simulator=simulator;
              	 	heuristicreach.HostGraphName=HostGraphName;
              	 	heuristicreach.EnableSelectedHostGraph();
              	 	
              	 	reportTime_EnableHostGraph2= System.currentTimeMillis() - startTime;
              	 	
              	 	
                }

                @Override
                public void refresh(Exploration exploration) {
                    setEnabled(Enable_HostGraph_TOOLTIP, exploration);
                }
            };
        }
        return this.EnableHostGraphButton2;
    }
    private RefreshButton getExploreButton() {
        if (this.ExploreButton== null) {
            // Create the explore button (reference is needed when setting the
            // initial value of the (strategy/acceptor) editors.
            this.ExploreButton = new RefreshButton(Explore_COMMAND) {
                @Override
                public void execute() {
                	  
                	if(heuristicreach==null)
                		heuristicreach=new HeuStyleUser();
                	
                
                	/////
                    EnableHostGraphButton.setEnabled(true);
                    ExploreButton.setEnabled(true);
                
                    MakeKnowlegeBaseButton.setEnabled(false);
                    EnableHostGraphButton2.setEnabled(false);
                    startButton.setEnabled(false);
                    manageRB();
                    ////
                	
                	
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

             	
               	
                
                	long startTime = System.currentTimeMillis();
            	    
                	/////////////////////////////////////
               	 	heuristicreach.simulator=simulator;
               	 	heuristicreach.Explore();
               	 	
               	 	reportTime_Exhau_Explo_Small= System.currentTimeMillis() - startTime;
                }

                @Override
                public void refresh(Exploration exploration) {
                    setEnabled(Explore_TOOLTIP, exploration);
                }
            };
        }
        return this.ExploreButton;
    }
    
    public long AllreportTime;
    public long reportTime_EnableHostGraph1;
    public long reportTime_EnableHostGraph2;
    public long reportTime_Data_Mining;  //Executing of the FindFreqPatt algorithm for data mining
    public long reportTime_Exhau_Explo_Small;   //Exhaustive exploring of the samller model
    public long reportTime_Effi_Explo_Large;  //Efficient exploring of the large model
    public long Number_Explored_States;   //Number of explored states
    
    
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
