/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package markiewb.nb.maven.project.setjdk;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

@ActionID(
        category = "Build",
        id = "markiewb.nb.maven.project.setjdk.SetJDKAction"
)
@ActionRegistration(
        displayName = "#CTL_SetJDKAction", lazy = false
)
@ActionReference(path = "Projects/Actions")
@Messages("CTL_SetJDKAction=SetJDKAction")
public final class SetJDKAction extends AbstractAction implements ActionListener, Presenter.Popup {

    private final Lookup.Result<Project> result;
    private final transient LookupListener lookupListener;
    private Collection<Project> context = new ArrayList<>();

    public SetJDKAction() {
        //disabled by default - at loading time
        setEnabled(false);
        //create an action, which is only enabled when >=1 projects are selected
        result = Utilities.actionsGlobalContext().lookupResult(Project.class);
        this.lookupListener = new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                context.clear();
                context.addAll(result.allInstances());

                final Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        int s = result.allInstances().size();
                        SetJDKAction.this.setEnabled(s > 0);
                    }
                };
                // to make sure that it will be executed on EDT
                if (EventQueue.isDispatchThread()) {
                    runnable.run();
                } else {
                    SwingUtilities.invokeLater(runnable);
                }
            }
        };
        result.addLookupListener(WeakListeners.create(LookupListener.class, this.lookupListener, result));
    }
    /**
     * Copied from {@link org.netbeans.modules.maven.api.Constants}
     */
    public static final String HINT_JDK_PLATFORM = "netbeans.hint.jdkPlatform"; //NOI18N

    @Override
    public void actionPerformed(ActionEvent ev) {
        //NOP
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem result = new JMenu("Set JDK");

        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
//        System.out.println("jpm.getDefaultPlatform() = " + jpm.getDefaultPlatform());
//        System.out.println("jpm.getInstalledPlatforms() = " + Arrays.asList(jpm.getInstalledPlatforms()));
        List<JavaPlatform> asList = Arrays.asList(jpm.getInstalledPlatforms());
        for (JavaPlatform jp : asList) {
            final String platformId = jp.getProperties().get("platform.ant.name");
//            System.out.println(String.format("javaPlatform = %s '%s' '%s' '%s'", jp, jp.getDisplayName(), jp.getVendor(), platformId));
//            System.out.println("jp = " + jp.getInstallFolders());
//            System.out.println("jp = " + jp.getProperties());
//            System.out.println("jp = " + jp.getSpecification());
//            System.out.println("jp = " + jp.getStandardLibraries());
            final String displayName = String.format("%s (%s)", jp.getDisplayName(), jp.getInstallFolders().iterator().next().getPath());
            final JMenuItem item = new JMenuItem(new AbstractAction(displayName) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("curr = " + platformId);
                    for (Project project : context) {
                        final AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
                        if (null != props) {
                            props.put(HINT_JDK_PLATFORM, platformId, true);
                        }
                    }
                }
            });

            result.add(item);
        }

        return result;
    }

}
