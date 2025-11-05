package org.sideloader;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;

public class HelpAbout extends JDialog {
    private JPanel panel1;
    private JTabbedPane tabbedPane;
    private JComboBox guideCombo;
    private JComboBox aboutCombo;
    private JTextArea infoArea;
    private JScrollPane textScroll;
    private JScrollPane textScroll2;
    private JTextArea infoArea2;
    private JPanel guidePanel;
    private JPanel aboutPanel;
    private JLabel tssIco;
    private String placeholder = "undefined";

    public HelpAbout() {
        setModal(true);
        setSize(new Dimension(500, 300));
        setMinimumSize(new Dimension(250, 200));
        add(panel1);
        guideCombo.setSelectedIndex(0);
        aboutCombo.setSelectedIndex(0);
        infoArea.setText("Welcome to Snap Sideloader!\nThis guide will give you an overview of basic usage.\nFirst off, the store UI. In the upper part " +
                "of the screen you can see a search bar and a search button. On the side you can see a few buttons as well as a list of categories, if toggled on. In the center is where" +
                "you will see the content pages. Some content pages will lead to other pages. Use the \"Home\" button on the sidebar to go back to the Front page.\n" +
                "When you're done, click the \"Exit\" button to close the program.");
        infoArea2.setText("The Snap Sideloader\n" +
                "Copyright 2024-2025 Andrei Ionel\n" +
                "\n" +
                "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                "you may not use this file except in compliance with the License.\n" +
                "You may obtain a copy of the License at\n" +
                "\n" +
                "    http://www.apache.org/licenses/LICENSE-2.0\n" +
                "\n" +
                "Unless required by applicable law or agreed to in writing, software\n" +
                "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                "See the License for the specific language governing permissions and\n" +
                "limitations under the License.\n\n" +
                "Source code available at: https://github.com/thetechdog/the-snap-sideloader");
        textScroll.getVerticalScrollBar().setValue(0);
        infoArea2.setCaretPosition(0);
        tssIco.setVisible(true);

        guideCombo.addActionListener(e -> {
            if (guideCombo.getSelectedIndex() == 0) {
                infoArea.setText("Welcome to Snap Sideloader!\nThis guide will give you an overview of basic usage.\nFirst off, the store UI. In the upper part " +
                        "of the screen you can see a search bar and a search button. On the side you can see a few buttons as well as a list of categories, if toggled on. In the center is where" +
                        "you will see the content pages. Some content pages will lead to other pages. Use the \"Home\" button on the sidebar to go back to the Front page.\n" +
                        "When you're done, click the \"Exit\" button to close the program.");
                textScroll.getVerticalScrollBar().setValue(0);
            } else if (guideCombo.getSelectedIndex() == 1) {
                infoArea.setText("Clicking on the Settings button located on the sidebar will open the settings menu. Here you can change your theme, repository update frequency," +
                        "enable/disable program suggestions and enable/disable the featured page. You can also start a manual repository update and access the Repository Manager.");
                textScroll.getVerticalScrollBar().setValue(0);
            }
        });
        aboutCombo.addActionListener(e -> {
            String aboutText = "";
            tssIco.setVisible(false);
            switch (aboutCombo.getSelectedIndex()) {
                case 0:
                    aboutText = "The Snap Sideloader\n" +
                            "Copyright 2024-2025 Andrei Ionel\n" +
                            "\n" +
                            "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                            "you may not use this file except in compliance with the License.\n" +
                            "You may obtain a copy of the License at\n" +
                            "\n" +
                            "    http://www.apache.org/licenses/LICENSE-2.0\n" +
                            "\n" +
                            "Unless required by applicable law or agreed to in writing, software\n" +
                            "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                            "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                            "See the License for the specific language governing permissions and\n" +
                            "limitations under the License.\n\n" +
                            "Source code available at: https://github.com/thetechdog/the-snap-sideloader";
                    tssIco.setVisible(true);
                    break;
                case 1:
                    aboutText = "              The BSD License for the JGoodies Forms\n" +
                            "              ======================================\n" +
                            "\n" +
                            "Copyright (c) 2002-2015 JGoodies Software GmbH. All rights reserved.\n" +
                            "\n" +
                            "Redistribution and use in source and binary forms, with or without \n" +
                            "modification, are permitted provided that the following conditions are met:\n" +
                            "\n" +
                            " o Redistributions of source code must retain the above copyright notice, \n" +
                            "   this list of conditions and the following disclaimer. \n" +
                            "    \n" +
                            " o Redistributions in binary form must reproduce the above copyright notice, \n" +
                            "   this list of conditions and the following disclaimer in the documentation \n" +
                            "   and/or other materials provided with the distribution. \n" +
                            "    \n" +
                            " o Neither the name of JGoodies Software GmbH nor the names of \n" +
                            "   its contributors may be used to endorse or promote products derived \n" +
                            "   from this software without specific prior written permission. \n" +
                            "    \n" +
                            "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" \n" +
                            "AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, \n" +
                            "THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR \n" +
                            "PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR \n" +
                            "CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, \n" +
                            "EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, \n" +
                            "PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; \n" +
                            "OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, \n" +
                            "WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR \n" +
                            "OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, \n" +
                            "EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
                    break;
                case 2:
                    aboutText = "Copyright 2008 Google Inc.\n" +
                            "\n" +
                            "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                            "you may not use this file except in compliance with the License.\n" +
                            "You may obtain a copy of the License at\n" +
                            "\n" +
                            "    http://www.apache.org/licenses/LICENSE-2.0\n" +
                            "\n" +
                            "Unless required by applicable law or agreed to in writing, software\n" +
                            "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                            "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                            "See the License for the specific language governing permissions and\n" +
                            "limitations under the License.";
                    break;
                case 3:
                    aboutText = "This product includes the following softwares developed by David Crawshaw.\n" +
                            "See LICENSE.zentus file.\n" +
                            "\n" +
                            "And also, NestedVM (Apache License Version 2.0) is used inside sqlite-\n" +
                            "Copyright 2025 Xerial and contributors\n" +
                            "\n" +
                            "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                            "   you may not use this file except in compliance with the License.\n" +
                            "   You may obtain a copy of the License at\n" +
                            "\n" +
                            "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                            "\n" +
                            "   Unless required by applicable law or agreed to in writing, software\n" +
                            "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                            "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                            "   See the License for the specific language governing permissions and\n" +
                            "   limitations under the License.\n" +
                            "Original version (BSD 2-Clause):\n" +
                            "Copyright (c) 2006, David Crawshaw.  All rights reserved.\n" +
                            "\n" +
                            "Redistribution and use in source and binary forms, with or without\n" +
                            "modification, are permitted provided that the following conditions\n" +
                            "are met:\n" +
                            "\n" +
                            "1. Redistributions of source code must retain the above copyright\n" +
                            "   notice, this list of conditions and the following disclaimer.\n" +
                            "2. Redistributions in binary form must reproduce the above copyright\n" +
                            "   notice, this list of conditions and the following disclaimer in the\n" +
                            "   documentation and/or other materials provided with the distribution.\n" +
                            "\n" +
                            "THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND\n" +
                            "ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE\n" +
                            "IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE\n" +
                            "ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE\n" +
                            "FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL\n" +
                            "DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS\n" +
                            "OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)\n" +
                            "HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT\n" +
                            "LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY\n" +
                            "OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF\n" +
                            "SUCH DAMAGE.";
                    break;
                case 4:
                    aboutText = "BSD 3-Clause License\n" +
                            "\n" +
                            "Copyright (c) 2008-2020, Harald Kuhr\n" +
                            "All rights reserved.\n" +
                            "\n" +
                            "Redistribution and use in source and binary forms, with or without\n" +
                            "modification, are permitted provided that the following conditions are met:\n" +
                            "\n" +
                            "* Redistributions of source code must retain the above copyright notice, this\n" +
                            "  list of conditions and the following disclaimer.\n" +
                            "\n" +
                            "* Redistributions in binary form must reproduce the above copyright notice,\n" +
                            "  this list of conditions and the following disclaimer in the documentation\n" +
                            "  and/or other materials provided with the distribution.\n" +
                            "\n" +
                            "* Neither the name of the copyright holder nor the names of its\n" +
                            "  contributors may be used to endorse or promote products derived from\n" +
                            "  this software without specific prior written permission.\n" +
                            "\n" +
                            "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"\n" +
                            "AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE\n" +
                            "IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE\n" +
                            "DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE\n" +
                            "FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL\n" +
                            "DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR\n" +
                            "SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER\n" +
                            "CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,\n" +
                            "OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\n" +
                            "OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
                    break;
                case 5:
                    aboutText = "Copyright 2019 Square, Inc.\n" +
                            "\n" +
                            "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                            "you may not use this file except in compliance with the License.\n" +
                            "You may obtain a copy of the License at\n" +
                            "\n" +
                            "   http://www.apache.org/licenses/LICENSE-2.0\n" +
                            "\n" +
                            "Unless required by applicable law or agreed to in writing, software\n" +
                            "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                            "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                            "See the License for the specific language governing permissions and\n" +
                            "limitations under the License.";
                    break;
                case 6:
                    aboutText = "Copyright 2025 Rkalla and contributors\n" +
                            "\n" +
                            "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                            "   you may not use this file except in compliance with the License.\n" +
                            "   You may obtain a copy of the License at\n" +
                            "\n" +
                            "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                            "\n" +
                            "   Unless required by applicable law or agreed to in writing, software\n" +
                            "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                            "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                            "   See the License for the specific language governing permissions and\n" +
                            "   limitations under the License.";
                    break;
                case 7:
                    aboutText = "Apache Commons Text\n" +
                            "Copyright 2014-2025 The Apache Software Foundation\n" +
                            "\n" +
                            "This product includes software developed at\n" +
                            "The Apache Software Foundation (https://www.apache.org/).\n\n" +
                            "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                            "   you may not use this file except in compliance with the License.\n" +
                            "   You may obtain a copy of the License at\n" +
                            "\n" +
                            "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                            "\n" +
                            "   Unless required by applicable law or agreed to in writing, software\n" +
                            "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                            "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                            "   See the License for the specific language governing permissions and\n" +
                            "   limitations under the License.";
                    break;
                default:
                    infoArea2.setText("Invalid combo box index!!");

            }
            infoArea2.setText(aboutText);
            infoArea2.setCaretPosition(0);
        });


        tabbedPane.remove(guidePanel);
        tabbedPane.add(new JLabel("Under construction..."));
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new FormLayout("fill:d:grow", "top:80px:grow"));
        tabbedPane = new JTabbedPane();
        tabbedPane.setEnabled(true);
        CellConstraints cc = new CellConstraints();
        panel1.add(tabbedPane, cc.xy(1, 1, CellConstraints.DEFAULT, CellConstraints.TOP));
        guidePanel = new JPanel();
        guidePanel.setLayout(new FormLayout("fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow", "center:d:grow,top:4dlu:noGrow,center:d:grow"));
        guidePanel.setEnabled(false);
        tabbedPane.addTab("User Guide", guidePanel);
        guideCombo = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Getting Around the UI");
        defaultComboBoxModel1.addElement("Configuration");
        defaultComboBoxModel1.addElement("Managing Repositories");
        defaultComboBoxModel1.addElement("Front Page");
        defaultComboBoxModel1.addElement("Searching");
        defaultComboBoxModel1.addElement("Program Overview");
        defaultComboBoxModel1.addElement("Program Management");
        defaultComboBoxModel1.addElement("Switching Repositories");
        defaultComboBoxModel1.addElement("Installing Local Packages");
        guideCombo.setModel(defaultComboBoxModel1);
        guidePanel.add(guideCombo, cc.xy(3, 1));
        final JLabel label1 = new JLabel();
        label1.setText("Pick a Category:");
        guidePanel.add(label1, cc.xy(1, 1));
        textScroll = new JScrollPane();
        textScroll.setHorizontalScrollBarPolicy(31);
        guidePanel.add(textScroll, cc.xyw(1, 3, 3, CellConstraints.FILL, CellConstraints.FILL));
        infoArea = new JTextArea();
        infoArea.setLineWrap(true);
        infoArea.setMinimumSize(new Dimension(705, 100));
        infoArea.setText("");
        infoArea.setWrapStyleWord(true);
        textScroll.setViewportView(infoArea);
        aboutPanel = new JPanel();
        aboutPanel.setLayout(new FormLayout("fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:grow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        tabbedPane.addTab("About", aboutPanel);
        aboutCombo = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("The Snap Sideloader");
        defaultComboBoxModel2.addElement("JGoodies Forms");
        defaultComboBoxModel2.addElement("Gson");
        defaultComboBoxModel2.addElement("SQLite JDBC");
        defaultComboBoxModel2.addElement("TwelveMonkeys ImageIO WebP");
        defaultComboBoxModel2.addElement("OkHttp3");
        defaultComboBoxModel2.addElement("ImgScalr");
        defaultComboBoxModel2.addElement("Apache Commons Text");
        aboutCombo.setModel(defaultComboBoxModel2);
        aboutPanel.add(aboutCombo, cc.xy(3, 1));
        final JLabel label2 = new JLabel();
        label2.setText("Pick an Item:");
        aboutPanel.add(label2, cc.xy(1, 1));
        textScroll2 = new JScrollPane();
        textScroll2.setHorizontalScrollBarPolicy(31);
        aboutPanel.add(textScroll2, cc.xyw(1, 3, 5, CellConstraints.FILL, CellConstraints.FILL));
        infoArea2 = new JTextArea();
        infoArea2.setLineWrap(true);
        infoArea2.setMinimumSize(new Dimension(705, 100));
        infoArea2.setText("");
        infoArea2.setWrapStyleWord(true);
        textScroll2.setViewportView(infoArea2);
        tssIco = new JLabel();
        tssIco.setIcon(new ImageIcon(getClass().getResource("/tss.png")));
        tssIco.setMaximumSize(new Dimension(24, 24));
        tssIco.setMinimumSize(new Dimension(24, 24));
        tssIco.setText("");
        tssIco.setToolTipText("The Snap Sideloader");
        tssIco.setVisible(false);
        aboutPanel.add(tssIco, cc.xy(5, 1));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}
