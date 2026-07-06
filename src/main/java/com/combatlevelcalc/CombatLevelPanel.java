package com.combatlevelcalc;

import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class CombatLevelPanel extends PluginPanel
{
    private final JTextField attackField = new JTextField(3);
    private final JTextField strengthField = new JTextField(3);
    private final JTextField defenceField = new JTextField(3);
    private final JTextField hitpointsField = new JTextField(3);
    private final JTextField rangedField = new JTextField(3);
    private final JTextField prayerField = new JTextField(3);
    private final JTextField magicField = new JTextField(3);
    private final JLabel combatLabel = new JLabel("Combat lvl: -");
    private final JCheckBox showUnattainable = new JCheckBox("Show unattainable builds", true);
    private final JButton fetchButton = new JButton("Fetch stats");
    private final JPanel buildsPanel = new JPanel();

    // Dark theme color palette - subtle colors that blend with RuneLite's UI
    private static final Color ATTAINABLE_ACCENT = new Color(100, 220, 120);
    private static final Color UNATTAINABLE_ACCENT = new Color(240, 120, 120);
    private static final Color ATTAINABLE_BG = new Color(35, 55, 35);
    private static final Color UNATTAINABLE_BG = new Color(55, 35, 35);
    private static final Color HEADER_TEXT = new Color(220, 220, 220);
    private static final Color BORDER_DARK = new Color(70, 70, 70);
    private static final Color DETAIL_BG = new Color(45, 45, 45);

    private final List<BuildModel> buildTemplates = BuildFactory.getBuilds();
    private ActionListener fetchActionListener;

    public CombatLevelPanel()
    {
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(0, 2));
        addLabeled(top, "Attack:", attackField);
        addLabeled(top, "Strength:", strengthField);
        addLabeled(top, "Defence:", defenceField);
        addLabeled(top, "Hitpoints:", hitpointsField);
        addLabeled(top, "Ranged:", rangedField);
        addLabeled(top, "Prayer:", prayerField);
        addLabeled(top, "Magic:", magicField);

        top.add(fetchButton);
        top.add(new JLabel(""));

        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());

        JPanel statusRow = new JPanel(new GridLayout(0, 1));
        statusRow.add(combatLabel);
        statusRow.add(showUnattainable);
        center.add(statusRow, BorderLayout.NORTH);

        buildsPanel.setLayout(new BoxLayout(buildsPanel, BoxLayout.Y_AXIS));
        JScrollPane buildsScroll = new JScrollPane(buildsPanel);
        buildsScroll.setPreferredSize(new Dimension(400, 220));
        center.add(buildsScroll, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        DocumentListener dl = new DocumentListener()
        {
            @Override public void insertUpdate(DocumentEvent e) { updateCombatLabel(); }
            @Override public void removeUpdate(DocumentEvent e) { updateCombatLabel(); }
            @Override public void changedUpdate(DocumentEvent e) { updateCombatLabel(); }
        };

        attackField.getDocument().addDocumentListener(dl);
        strengthField.getDocument().addDocumentListener(dl);
        defenceField.getDocument().addDocumentListener(dl);
        hitpointsField.getDocument().addDocumentListener(dl);
        rangedField.getDocument().addDocumentListener(dl);
        prayerField.getDocument().addDocumentListener(dl);
        magicField.getDocument().addDocumentListener(dl);

        showUnattainable.addActionListener(e -> refreshBuilds());
        fetchButton.addActionListener(e -> {
            if (fetchActionListener != null)
            {
                fetchActionListener.actionPerformed(e);
            }
        });
    }

    private void addLabeled(JPanel p, String label, JComponent comp)
    {
        p.add(new JLabel(label));
        p.add(comp);
    }

    private int parse(JTextField f)
    {
        try
        {
            return Integer.parseInt(f.getText().trim());
        }
        catch (Exception ex)
        {
            return 0;
        }
    }

    private StatsModel getCurrentStats()
    {
        return new StatsModel(
            parse(attackField),
            parse(strengthField),
            parse(defenceField),
            parse(hitpointsField),
            parse(rangedField),
            parse(prayerField),
            parse(magicField)
        );
    }

    private void updateCombatLabel()
    {
        StatsModel s = getCurrentStats();
        int combat = CombatCalculator.computeCombatLevel(s);
        combatLabel.setText("Combat: " + combat);
        refreshBuilds();
    }

    private void refreshBuilds()
    {
        buildsPanel.removeAll();
        StatsModel current = getCurrentStats();
        List<BuildModel> builds = BuildFactory.evaluateBuilds(current, buildTemplates);

        List<BuildModel> attainable = new ArrayList<>();
        List<BuildModel> unattainable = new ArrayList<>();

        for (BuildModel build : builds)
        {
            if (build.isReachable())
            {
                attainable.add(build);
            }
            else
            {
                unattainable.add(build);
            }
        }

        if (!attainable.isEmpty())
        {
            JLabel header = createSectionHeader("Attainable Builds (" + attainable.size() + ")", ATTAINABLE_ACCENT);
            buildsPanel.add(header);

            for (BuildModel build : attainable)
            {
                buildsPanel.add(createBuildCard(build, true));
            }
        }

        if (showUnattainable.isSelected() && !unattainable.isEmpty())
        {
            JLabel header = createSectionHeader("Unattainable Builds (" + unattainable.size() + ")", UNATTAINABLE_ACCENT);
            buildsPanel.add(header);

            for (BuildModel build : unattainable)
            {
                buildsPanel.add(createBuildCard(build, false));
            }
        }

        if (attainable.isEmpty() && unattainable.isEmpty())
        {
            JLabel empty = new JLabel("No builds available");
            empty.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
            buildsPanel.add(empty);
        }

        buildsPanel.revalidate();
        buildsPanel.repaint();
    }

    private static JLabel createSectionHeader(String text, Color accent)
    {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Dialog", Font.BOLD, 12));
        label.setForeground(accent);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, accent),
            BorderFactory.createEmptyBorder(10, 4, 4, 4)));
        return label;
    }

    private JPanel createBuildCard(BuildModel build, boolean reachable)
    {
        Color accent = reachable ? ATTAINABLE_ACCENT : UNATTAINABLE_ACCENT;
        Color bg = reachable ? ATTAINABLE_BG : UNATTAINABLE_BG;

        // Main card with rounded-style look (subtle shadow via compound border)
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_DARK),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        // Header — always visible, clickable, with left accent stripe
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(bg);
        header.setOpaque(true);
        header.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));

        // Left accent bar
        JPanel accentBar = new JPanel();
        accentBar.setPreferredSize(new Dimension(3, 1));
        accentBar.setBackground(accent);
        accentBar.setOpaque(true);

        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.setOpaque(false);
        namePanel.add(accentBar, BorderLayout.WEST);

        JLabel nameLabel = new JLabel("  " + build.getName());
        nameLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        nameLabel.setForeground(HEADER_TEXT);
        namePanel.add(nameLabel, BorderLayout.CENTER);

        header.add(namePanel, BorderLayout.WEST);

        JLabel statusLabel = new JLabel((reachable ? "Reachable" : "Unattainable") + " \u00B7 CL " + build.getEstimatedCombatLevel());
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 10));
        statusLabel.setForeground(accent);
        header.add(statusLabel, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        // Detail panel — collapsed by default, with subtle background
        JPanel detail = new JPanel();
        detail.setLayout(new BoxLayout(detail, BoxLayout.Y_AXIS));
        detail.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        detail.setVisible(false);
        detail.setBackground(DETAIL_BG);
        detail.setOpaque(true);

        StringBuilder minText = new StringBuilder("Min: ");
        build.getMinimumStats().forEach((stat, value) -> minText.append(stat).append(" ").append(value).append(", "));
        detail.add(detailLabel(minText.toString(), HEADER_TEXT));


        if (!build.getMaximumStats().isEmpty())
        {
            StringBuilder maxText = new StringBuilder("Max: ");
            build.getMaximumStats().forEach((stat, value) -> maxText.append(stat).append(" ").append(value).append(", "));
            detail.add(detailLabel(maxText.toString(), HEADER_TEXT));
        }

        Map<String, Integer> delta = build.getDelta();
        if (!delta.isEmpty())
        {
            StringBuilder needText = new StringBuilder(reachable ? "Needs: " : "Missing: ");
            delta.forEach((stat, value) -> needText.append(stat).append(" +").append(value).append(", "));
            detail.add(detailLabel(needText.toString(), accent));
        }

        // Blocked stats (exceeding max caps)
        Map<String, Integer> blocked = build.getBlockedStats();
        if (!blocked.isEmpty())
        {
            StringBuilder blockText = new StringBuilder("Blocked: ");
            blocked.forEach((stat, value) -> blockText.append(stat).append(" -").append(value).append(", "));
            detail.add(detailLabel(blockText.toString(), UNATTAINABLE_ACCENT));
        }

        if (delta.isEmpty() && blocked.isEmpty())
        {
            detail.add(detailLabel("This build is already achieved!", ATTAINABLE_ACCENT));
        }

        card.add(detail, BorderLayout.CENTER);

        header.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e)
            {
                detail.setVisible(!detail.isVisible());
                card.revalidate();
                card.repaint();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e)
            {
                header.setCursor(new Cursor(Cursor.HAND_CURSOR));
                header.setBackground(reachable ? new Color(220, 245, 220) : new Color(245, 220, 220));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e)
            {
                header.setBackground(bg);
            }
        });

        // Wrapper for card spacing
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        wrapper.add(card, BorderLayout.CENTER);

        return wrapper;
    }

    private static JLabel detailLabel(String text, Color color)
    {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Dialog", Font.PLAIN, 11));
        label.setForeground(color);
        label.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        return label;
    }

    public void setStats(StatsModel s)
    {
        attackField.setText(String.valueOf(s.getAttack()));
        strengthField.setText(String.valueOf(s.getStrength()));
        defenceField.setText(String.valueOf(s.getDefence()));
        hitpointsField.setText(String.valueOf(s.getHitpoints()));
        rangedField.setText(String.valueOf(s.getRanged()));
        prayerField.setText(String.valueOf(s.getPrayer()));
        magicField.setText(String.valueOf(s.getMagic()));
        updateCombatLabel();
    }

    public void setFetchAction(ActionListener fetchActionListener)
    {
        this.fetchActionListener = fetchActionListener;
    }
}