package groove.gui.action;

import groove.grammar.model.GrammarModel;
import groove.gui.Options;
import groove.gui.Simulator;
import groove.gui.dialog.HeuGADialog;



/** Action to open the Exploration Dialog. */
public class HeuGADialogAction extends SimulatorAction {
    /** Constructs an instance of the action. */
    public HeuGADialogAction(Simulator simulator) {
        super(simulator, Options.HeuristicReachabilityGA_DIALOG_ACTION_NAME,
           null);
    }

    @Override
    public void execute() {
        new HeuGADialog(getSimulator(), getFrame());
       
    }

    
    
    @Override
    public void refresh() {
        GrammarModel grammar = getSimulatorModel().getGrammar();
        setEnabled(grammar != null && !grammar.hasErrors()
            && grammar.hasRules());
    }
}