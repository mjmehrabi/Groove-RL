package groove.gui.action;

import groove.grammar.model.GrammarModel;
import groove.gui.Options;
import groove.gui.Simulator;
import groove.gui.dialog.HeuBOADialog;



/** Action to open the Exploration Dialog. */
public class HeuBOADialogAction extends SimulatorAction {
    /** Constructs an instance of the action. */
    public HeuBOADialogAction(Simulator simulator) {
        super(simulator, Options.HeuristicReachabilityBOA_DIALOG_ACTION_NAME,
           null);
    }

    @Override
    public void execute() {
        new HeuBOADialog(getSimulator(), getFrame());
       
    }

    
    
    @Override
    public void refresh() {
        GrammarModel grammar = getSimulatorModel().getGrammar();
        setEnabled(grammar != null && !grammar.hasErrors()
            && grammar.hasRules());
    }
}