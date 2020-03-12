
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
import groove.verify.HeuLearnFromBFS;



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


public class HeuLearnFromBFSDialog extends JDialog  {

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

    /**
     * Create the dialog.
     * @param simulator - reference to the simulator
     * @param owner - reference to the parent GUI component
     * @throws FormatException
     */
    public HeuLearnFromBFSDialog(Simulator simulator, JFrame owner) {
    	
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
       	dialogContent.add(createRBmodelcheckingLivenessPanel());
       	
       	JLabel line2=new JLabel("------------------------------------------------------------------------------------");
    	line2.setForeground(Color.green);
    	dialogContent.add(line2);
       	
    
       	
        dialogContent.add(createModelChecking());
        
        
        
        
        
        dialogContent.add(createMaxnumberofStates());
        
        
        dialogContent.add(createRBTypeOfLearnPanel());
        
       
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
        
        dialogContent.add(createCancelPanel());
        
        //manageRB();
        
        SpringUtilities.makeCompactGrid(dialogContent, 17, 1, 5, 5, 15, 0);
        // Add the dialogContent to the dialog.
        add(dialogContent);
        setTitle("Model Checking by Learning a Bayesian Network & Data Mining(from exploring n states by BFS) ...");
        setIconImage(Icons.GROOVE_ICON_16x16.getImage());
        setSize(890, 580);   //width   height
        //pack();
        setLocationRelativeTo(owner);
        
        //setAlwaysOnTop(true);
        
        
        StartButton.setEnabled(true);
        
        setVisible(true);
        
        
        
     }
    private JPanel createRBTypeOfLearnPanel() {
    	JPanel LearnPanel = new JPanel();
    	
    	LearnPanel.add(new JLabel("Learning:"));
    	
    	rbLearnBN=new JRadioButton("Bayesian Network");
    	rbLearnDM=new JRadioButton("Data Mining");
    	
    	
    	rbLearnBN.setSelected(true);
    	LearnPanel.add(rbLearnBN);
    	LearnPanel.add(rbLearnDM);
    	
    	
    	
    	
    	ButtonGroup options = new ButtonGroup();
    	options.add(rbLearnBN);
    	options.add(rbLearnDM);
    	
    	JLabel jstep11=new JLabel("     Minimum support percentage (minsup):");
    	LearnPanel.add(jstep11);
        
        txtMinsup=new JTextField(5);
        txtMinsup.setText("0.6");
        LearnPanel.add(txtMinsup);
       
        
        JLabel jstep12=new JLabel(" maxDepth:");
    	LearnPanel.add(jstep12);
        
        txtmaxDepth=new JTextField(5);
        txtmaxDepth.setText("500");
        
        LearnPanel.add(txtmaxDepth);
        
        return LearnPanel;
    }
    private JPanel createRBmodelcheckingPanel() {
    	JPanel buttonPanel = new JPanel();
    	
    	rbdeadlock=new JRadioButton("Deadlock (EF q)");
    	rbreachability=new JRadioButton("Reachabiliy (EF q)");
    	rbsafetyBydeadlcok=new JRadioButton("Refutation of AG !deadlock");
    	rbsafetyByReach=new JRadioButton("Refutation of AG !q");
    	
    	
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
	   	rbsafetyBydeadlcok.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {             
           	 manageRB();
            }           
         });
   	
	   	rbsafetyByReach.addItemListener(new ItemListener() {
	           public void itemStateChanged(ItemEvent e) {             
	          	 manageRB();
	           }           
	     });
    	
    	buttonPanel.add(rbdeadlock);
    	buttonPanel.add(rbreachability);
    	buttonPanel.add(rbsafetyBydeadlcok);
    	buttonPanel.add(rbsafetyByReach);
    	rbdeadlock.setSelected(true);
    	    	
    	return buttonPanel;
    }
    private JPanel createRBmodelcheckingLivenessPanel() {
    	JPanel buttonPanel = new JPanel();
    	
       	rbliveBydeadlock=new JRadioButton("Refutation of AF q");
    	rbliveByCycle=new JRadioButton("Refutation of AF q");
    	
    	
    	rbliveBydeadlock.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {             
           	 manageRB();
            }           
         });
   	
    	rbliveByCycle.addItemListener(new ItemListener() {
	           public void itemStateChanged(ItemEvent e) {             
	          	 manageRB();
	           }           
	     });
    	
    	
    	buttonPanel.add(rbliveBydeadlock);
    	buttonPanel.add(rbliveByCycle);
    
    	
    	ButtonGroup options = new ButtonGroup();
    	options.add(rbdeadlock);
    	options.add(rbreachability);
    	options.add(rbsafetyBydeadlcok);
    	options.add(rbsafetyByReach);
    	options.add(rbliveBydeadlock);
    	options.add(rbliveByCycle);
    	
    	rbliveBydeadlock.setVisible(false);
    	
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
    
    
    private JPanel createMaxnumberofStates(){
    	JPanel buttonPanel = new JPanel();
    	JLabel jstep11=new JLabel("Use a BFS strategy to explore the state space until the total number of states will be:");
        //jstep11.setForeground(Color.red);
        buttonPanel.add(jstep11);
        
        txtMaxNumberOfStates=new JTextField(5);
        txtMaxNumberOfStates.setText("5000");
        buttonPanel.add(txtMaxNumberOfStates);
        
        buttonPanel.add(new JLabel("Iterations"));
        txtIterations=new JTextField(10);
        txtIterations.setText("100");
        buttonPanel.add(txtIterations);
        
        return buttonPanel;
    }

    private void manageRB(){
    	if(StartButton!=null){
    		StartButton.setEnabled(true);
            
            
        	
        	cmbModelCheckingType.removeAllItems();
           	cmbModelCheckingType.setEnabled(true);
    	
           	if(rbdeadlock.isSelected() || rbsafetyBydeadlcok.isSelected()){
        		cmbModelCheckingType.addItem("DeadLock");
        		cmbModelCheckingType.setEnabled(false);
        	}
           	
           	if(rbreachability.isSelected() || rbsafetyByReach.isSelected()){
        		for(int i=0;i<=Alltype.size()-1;i++){
        			String s=Alltype.get(i).toString();
        			if(!s.contains("Live") && !s.contains("live")){
        				cmbModelCheckingType.addItem(s);
        			}
        				
        		}
        	}
           	
                        	
        	if(rbliveByCycle.isSelected() || rbliveBydeadlock.isSelected()){
        		for(int i=0;i<=Alltype.size()-1;i++){
        			String s=Alltype.get(i).toString();
        			if(s.contains("Live") || s.contains("live")){
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
    
    private RefreshButton selectAoption;
    
    ArrayList<QualName> Alltype;
    
    
    private Simulator simulator;
    private HeuLearnFromBFS heuristicreach;
   
   
    
   
    private JTextField txtresultOfmodelchecking;
    private JTextField txtTimeSpent;
    private JComboBox cmbModelCheckingType;
    private JTextField txtMaxNumberOfStates;
    private JTextField txtMinsup;
    private JTextField txtmaxDepth;
    private JTextField txtIterations;
    private  JComboBox cmbHostGraph;
    
    
    
    private  JRadioButton rbdeadlock;
    private  JRadioButton rbreachability;
    private  JRadioButton rbsafetyBydeadlcok;
    private  JRadioButton rbsafetyByReach;
    private  JRadioButton rbliveBydeadlock;
    private  JRadioButton rbliveByCycle;
    
    private  JRadioButton rbLearnBN;
    private  JRadioButton rbLearnDM;
    
    
    private RefreshButton getEnableHostGraphButton() {
        if (this.EnableHostGraphButton == null) {
            // Create the explore button (reference is needed when setting the
            // initial value of the (strategy/acceptor) editors.
            this.EnableHostGraphButton = new RefreshButton(Enable_HostGraph_COMMAND) {
                @Override
                public void execute() {
                	  
                	//if(heuristicreach==null)
                		heuristicreach=new HeuLearnFromBFS();
                	
               	 	
               	 	
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
            	 	heuristicreach.simulator=simulator;
            	 	heuristicreach.HostGraphName=HostGraphName;
            	 	heuristicreach.EnableSelectedHostGraph();
            	 	
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
               	 	
                	long startTime = System.currentTimeMillis();
                	
                	if(heuristicreach==null)
                		heuristicreach=new HeuLearnFromBFS();
                	
                	String modelcheckingType =cmbModelCheckingType.getSelectedItem().toString();

                	
                	
                	                    
                   	heuristicreach.CTLproperty ="deadlock";
                   	if(rbreachability.isSelected())
                   		heuristicreach.CTLproperty ="reachability";
                   	if(rbdeadlock.isSelected() || rbsafetyBydeadlcok.isSelected()){
                   		heuristicreach.CTLproperty="deadlock";
                   		modelcheckingType="deadlock";
                   	}
                   	
                   	if(rbsafetyByReach.isSelected())
                   		heuristicreach.CTLproperty ="safetyByReach";
                	if(rbliveByCycle.isSelected())
                   		heuristicreach.CTLproperty ="liveByCycle";
                   	if(rbliveBydeadlock.isSelected()){
                   		heuristicreach.CTLproperty ="liveByDeadlock";
                   	}
                	
                	heuristicreach.ModelCheckingTarget=modelcheckingType;
                   	
                	
                   	heuristicreach.Alltype=Alltype;
                   	
                   	heuristicreach.minsup=Double.parseDouble(txtMinsup.getText());
                   	heuristicreach.maxDepth=Integer.parseInt(txtmaxDepth.getText());
                   	                   	
                   	setCursor(new Cursor(Cursor.WAIT_CURSOR));
                	
                	heuristicreach.simulator=simulator;
                	
                	String typeOfLearn="";
                	if(rbLearnBN.isSelected())
                		typeOfLearn="BN";
                	if(rbLearnDM.isSelected())
                		typeOfLearn="DM";
                	
                	
                	//final Runtime runTime = Runtime.getRuntime();
                	//startUsedMemory = runTime.totalMemory() - runTime.freeMemory();
                	
               	 	String s=heuristicreach.Explore(Integer.valueOf(txtIterations.getText()),Integer.valueOf(txtMaxNumberOfStates.getText()),modelcheckingType,RulesCount,RulesName,typeOfLearn,null,null);
               	 	
               	 	//long usedMemory = runTime.totalMemory() - runTime.freeMemory();
               	 	//long RealusedMemory=(usedMemory - startUsedMemory)/1024;
               	 	
               	 	
               	 	              	 	
                	
               	    String heuristicResult=heuristicreach.exploringItems.heuristicResult;
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
               	   
              	    
               	    
              	    
              	  if(rbdeadlock.isSelected()){
              		  if(heuristicResult.equals("reachability"))
              			  heuristicResult="The propery is verified"+"..Last state is: "+lastStateInReachability;
                    	else
                    	  heuristicResult="The propery is not verified.";
              	   }
              	   if(rbreachability.isSelected()){
              		 if(heuristicResult.equals("reachability"))
                		   heuristicResult="The propery is verified."+"..Last state is: "+lastStateInReachability;
                  	else
                  		heuristicResult="The propery is not verified.";
            	   }
	               if(rbsafetyBydeadlcok.isSelected() ){
	           		  if(heuristicResult.equals("reachability"))
	                 	   	heuristicResult="The propery is refuted"+"..Last state is: "+lastStateInReachability;
	                 	 else
	                 	   	heuristicResult="The propery is not refuted.";
	           	   }
	               if(rbsafetyByReach.isSelected() ){
		           		  if(heuristicResult.equals("reachability"))
		                 	   	heuristicResult="The propery is refuted"+"..Last state is: "+lastStateInReachability;
		                 	 else
		                 	   	heuristicResult="The propery is not refuted.";
		           	   }
	               	
	               	if(rbliveBydeadlock.isSelected() ){
		           		  if(heuristicResult.equals("reachability"))
		                 	   	heuristicResult="The propery is refuted."+"..Last state is: "+lastStateInReachability;
		                 	 else
		                 	   	heuristicResult="The propery is not refuted.";
		           	 }

	               	if(rbliveByCycle.isSelected() ){
		           		  if(heuristicResult.equals("reachability"))
		                 	   	heuristicResult="The propery is refuted."+"..Last state is: "+lastStateInReachability;
		                 	 else
		                 	   	heuristicResult="The propery is not refuted.";
		           	 }

              	    
              	    
               	    txtresultOfmodelchecking.setText(heuristicResult);
               	    long reportTime= System.currentTimeMillis() - startTime;
               	    String spenttime= String.valueOf(reportTime/1000)+" second :"+ String.valueOf(reportTime % 1000) +" millisecond ";
               	    
               	    
               	    
               	    String S1=String.valueOf(reportTime/1000.0);
            	    String S2=String.valueOf(heuristicreach.Number_Explored_States);
            	    String S3=String.valueOf(heuristicreach.First_Found_Dead_depth);
            	  
               	    
               	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                	
               	    if(flag)
            	    	txtTimeSpent.setText(S1+" , " + S2 +" , " +S3 );
            	    else	
            	    	txtTimeSpent.setText("");
            	    
            	    
               	    reportTime=0;
                	
                	if(heuristicreach==null)
                		heuristicreach=new HeuLearnFromBFS();
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
