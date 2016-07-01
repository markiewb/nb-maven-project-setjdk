/*
 * Copyright 2016 markiewb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.markiewb.netbeans.plugins.maven.project.setjdk;

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
        category = "Maven",
        id = "markiewb.nb.maven.project.setjdk.SetJDKAction"
)
@ActionRegistration(
        displayName = "#CTL_SetJDKAction", lazy = false
)
@ActionReference(position = 1550, path = "Projects/org-netbeans-modules-maven/Actions")
@Messages("CTL_SetJDKAction=Set JDK")
public final class SetJDKAction extends AbstractAction implements ActionListener, Presenter.Popup {

    /**
     * Copied from {@link org.netbeans.modules.maven.api.Constants}
     */
    public static final String HINT_JDK_PLATFORM = "netbeans.hint.jdkPlatform"; //NOI18N

    private Collection<Project> context = new ArrayList<>();
    private final transient LookupListener lookupListener;
    private final Lookup.Result<Project> result;

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

    @Override
    public void actionPerformed(ActionEvent ev) {
        //NOP
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem result = new JMenu(Bundle.CTL_SetJDKAction());
            
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        List<JavaPlatform> asList = Arrays.asList(jpm.getInstalledPlatforms());
        for (JavaPlatform jp : asList) {
            final String platformId = getPlatformId(jp);
            final String displayName = getDisplayName(jp);
            if (isBlank(platformId) || isBlank(displayName)) {
                continue;
            }
            final JMenuItem item = new JMenuItem(new AbstractAction(displayName) {
                @Override
                public void actionPerformed(ActionEvent e) {
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

    private boolean isBlank(String string) {
        return "".equals(string) || null == string;
    }

    private static String getDisplayName(JavaPlatform jp) {
        if (null == jp) {
            return null;
        }
        if (null == jp.getInstallFolders()) {
            return null;
        }
        return String.format("%s (%s)", jp.getDisplayName(), jp.getInstallFolders().iterator().next().getPath());
    }

    private static String getPlatformId(JavaPlatform jp) {
        return jp.getProperties().get("platform.ant.name");
    }
}
