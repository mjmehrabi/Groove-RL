package groove.gui.action;

import groove.grammar.model.GrammarModel;

import groove.gui.Options;
import groove.gui.Simulator;

import groove.gui.dialog.HeuPSODialog;



/** Action to open the Exploration Dialog. */
public class HeuPSODialogAction extends SimulatorAction {
    /** Constructs an instance of the action. */
    public HeuPSODialogAction(Simulator simulator) {
        super(simulator, Options.HeuristicReachabilityPSO_DIALOG_ACTION_NAME,
           null);
    }

    @Override
    public void execute() {
        new HeuPSODialog(getSimulator(), getFrame());
       
    }

    
    
    @Override
    public void refresh() {
        GrammarModel grammar = getSimulatorModel().getGrammar();
        setEnabled(grammar != null && !grammar.hasErrors()
            && grammar.hasRules());
    }
}