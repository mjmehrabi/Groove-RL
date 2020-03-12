
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
import groove.verify.HeuIDAstar;


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


public class HeuIDAstarDialog extends JDialog  {

	private static final String START_COMMAND = "Start";
	private static final String Make_Knowlege_Base_COMMAND = "Make Knowlege Base";
	private static final String Enable_HostGraph_COMMAND = "Enable the Selected HostGraph";
	private static final String Explore_COMMAND = "Exploring & Model Checking";
	
	
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
    public HeuIDAstarDialog(Simulator simulator, JFrame owner) {
    	
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
       	
      
       	
       	dialogContent.add(createModelChecking());
       	
       	dialogContent.add(createRBTypeOfAlgPanel());
       	
       	
    
        
        dialogContent.add(createTextBoxes());
        
        
       
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
        
        manageRB();
        
        SpringUtilities.makeCompactGrid(dialogContent, 15, 1, 5, 5, 15, 0);
        // Add the dialogContent to the dialog.
        add(dialogContent);
        setTitle("Model Checking by Iterative deepening A* (IDA*) and Beam Search ...");
        setIconImage(Icons.GROOVE_ICON_16x16.getImage());
        setSize(800, 560);   //width   height
        //pack();
        setLocationRelativeTo(owner);
        
        StartButton.setEnabled(false);
        
        
        //setAlwaysOnTop(true);
        
        setVisible(true);
        
        
        
     }
    

 
    
    private JPanel createRBTypeOfAlgPanel() {
    	JPanel LearnPanel = new JPanel();
    	
    	
    	
    	//rbAstar=new JRadioButton("A*");
    	rbIDAstar=new JRadioButton("Iterative deepening A* (IDA*)");
    	rbBeamSearch=new JRadioButton("Beam Search");
    	
    	
    	rbIDAstar.setSelected(true);
    	//LearnPanel.add(rbAstar);
    	LearnPanel.add(rbIDAstar);
    	LearnPanel.add(rbBeamSearch);
    	
    	
    	
    	ButtonGroup options = new ButtonGroup();
    	//options.add(rbAstar);
    	options.add(rbIDAstar);
    	options.add(rbBeamSearch);
    	
        return LearnPanel;
    }
    
    private JPanel createRBTypeOfheuristics() {
    	JPanel HeuPanel = new JPanel();
    	
    	HeuPanel.add(new JLabel("Heuristics:"));
    	
    	rbBlkRulesInPath=new JRadioButton("BlockedRulesInPath");
    	rbBlkRulesInState=new JRadioButton("BlockedRulesInState");
    	
    	rbBlkRulesInPath.setSelected(true);
    	HeuPanel.add(rbBlkRulesInPath);
    
    	rbBlkRulesInState.setSelected(true);
    	HeuPanel.add(rbBlkRulesInState);
    
    	
    	
    	ButtonGroup options = new ButtonGroup();
    	options.add(rbBlkRulesInPath);
    	options.add(rbBlkRulesInState);
    	
        return HeuPanel;
    }
    private JPanel createRBmodelcheckingPanel() {
    	JPanel buttonPanel = new JPanel();
    	
    	rbdeadlock=new JRadioButton("Deadlock (EF q)");
    	rbreachability=new JRadioButton("Reachabiliy (EF q)");
    	//rbsafetyBydeadlcok=new JRadioButton("Refutation of AG !deadlock");
    	//rbsafetyByReach=new JRadioButton("Refutation of AG !q");
    	
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
	   	
    	
    	buttonPanel.add(rbdeadlock);
    	buttonPanel.add(rbreachability);
    	//buttonPanel.add(rbsafetyBydeadlcok);
    	//buttonPanel.add(rbsafetyByReach);
    	
    	
    	ButtonGroup options = new ButtonGroup();
    	options.add(rbdeadlock);
    	options.add(rbreachability);
    	    	
    	rbdeadlock.setSelected(true);
    	    	
    	return buttonPanel;
    }
    private void manageRB(){
    	if(StartButton!=null){
	
        	cmbModelCheckingType.removeAllItems();
           	cmbModelCheckingType.setEnabled(true);
    	
           	if(rbdeadlock.isSelected()){
        		cmbModelCheckingType.addItem("deadlock");
        		cmbModelCheckingType.setEnabled(false);
        	}
           	
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
    
    
    private JPanel createTextBoxes(){
    	JPanel buttonPanel = new JPanel();
            
       
        
        buttonPanel.add(new JLabel("Depth of Search"));
        txtDepthOfSearch=new JTextField(5);
        txtDepthOfSearch.setText("100");
        buttonPanel.add(txtDepthOfSearch);
        
        buttonPanel.add(new JLabel("Beam Width"));
        txtBeamWith=new JTextField(5);
        txtBeamWith.setText("10");
        buttonPanel.add(txtBeamWith);
        
        return buttonPanel;
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
       
        cmbModelCheckingType.addItem("deadlock");
        
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
    private HeuIDAstar heuristicreach;
   
   
    
   
    private JTextField txtresultOfmodelchecking;
    private JTextField txtTimeSpent;
    private JComboBox cmbModelCheckingType;
    
  
   
    private  JComboBox cmbHostGraph;
    private JTextField txtDepthOfSearch;
    private JTextField txtBeamWith;
    
    private  JRadioButton rbdeadlock;
    private  JRadioButton rbreachability;
   
    
    private  JRadioButton rbAstar;
    private  JRadioButton rbIDAstar;
    private  JRadioButton rbBeamSearch;
    
    private  JRadioButton rbBlkRulesInPath;
    private  JRadioButton rbBlkRulesInState;
    
    
    
    
    private RefreshButton getEnableHostGraphButton() {
        if (this.EnableHostGraphButton == null) {
            // Create the explore button (reference is needed when setting the
            // initial value of the (strategy/acceptor) editors.
            this.EnableHostGraphButton = new RefreshButton(Enable_HostGraph_COMMAND) {
                @Override
                public void execute() {
                	  
        		heuristicreach=new HeuIDAstar();
        		Alltype=new ArrayList<QualName>();
        		
                                   	
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
        	        				// TODO Auto-generated catch block
        	        				e.printStackTrace();
        	        			}
        	        		}
        	        		
        	        		if(!flag)
        	        			Alltype.add(ts);
        	        		else{
        	        			RulesCount++;
        	        			RulesName.add(ts);
        	        			
        	        		}
        	        	}
        	       	}
                       	
            		               	 	
               	    String HostGraphName;
               	    HostGraphName=cmbHostGraph.getSelectedItem().toString();
            	 	heuristicreach.simulator=simulator;
            	 	heuristicreach.HostGraphName=HostGraphName;
            	 	heuristicreach.EnableSelectedHostGraph();
            	 	
            	 	 StartButton.setEnabled(true);
            	 	
                }

                @Override
                public void refresh(Exploration exploration) {
                    setEnabled(Enable_HostGraph_TOOLTIP, exploration);
                }
            };
        }
        return this.EnableHostGraphButton;
    }
    public void FillModelCheckingType(ArrayList<String> alltype){
    	//cmbModelCheckingType.removeAll();
    	cmbModelCheckingType.removeAllItems();
    	for(int i=0;i<=alltype.size()-1;i++){
    		String s=alltype.get(i);
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
                		heuristicreach=new HeuIDAstar();
                	
                	String modelcheckingType ="deadlock";

                	
                	
                	                    
                   	heuristicreach.CTLproperty ="deadlock";
                   	modelcheckingType =cmbModelCheckingType.getSelectedItem().toString();
                   	if(rbreachability.isSelected())
                   		heuristicreach.CTLproperty ="reachability";
                   	if(rbdeadlock.isSelected()){
                   		heuristicreach.CTLproperty="deadlock";
                   		modelcheckingType="deadlock";
                   	}
                   	
                	heuristicreach.typeOfAlg ="IDA*";
                   	if(rbIDAstar.isSelected()){
                   		heuristicreach.typeOfAlg ="IDA*";
                   	}
                   	if(rbBeamSearch.isSelected()){
                   		heuristicreach.typeOfAlg ="BeamSearch";
                   	}
                   	
                   	
                	
                	heuristicreach.ModelCheckingTarget=modelcheckingType;
                   	
                	
                   	heuristicreach.Alltype=Alltype;
                   
                   	heuristicreach.maxDepthOfSearch=Integer.parseInt(txtDepthOfSearch.getText());
                   	heuristicreach.BeamWidth=Integer.parseInt(txtBeamWith.getText());                   	
                   	
                   	setCursor(new Cursor(Cursor.WAIT_CURSOR));
                	
                	heuristicreach.simulator=simulator;
                	
                	String typeOfHeuristic="";
                	//if(rbBlkRulesInPath.isSelected())
                		//typeOfHeuristic="HEU_BLKRULESPATH";
                	//if(rbBlkRulesInState.isSelected())
                		typeOfHeuristic="HEU_BLKRULESSTATE";
                	
                	
                	//final Runtime runTime = Runtime.getRuntime();
                	//startUsedMemory = runTime.totalMemory() - runTime.freeMemory();
                	
               	 	String s=heuristicreach.Explore(1,modelcheckingType,RulesCount,RulesName,typeOfHeuristic,null,null);
               	 	
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
                		heuristicreach=new HeuIDAstar();
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
