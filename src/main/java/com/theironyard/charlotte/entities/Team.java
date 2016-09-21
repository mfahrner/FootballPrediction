package com.theironyard.charlotte.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mfahrner on 9/18/16.
 */
@Entity
@Table(name = "teams")
public class Team {

    double constant = -0.36;
    double homeField = 0.72;
    double offPassCo = 0.46;
    double offRunCo = 0.25;
    double offIntCo = -19.4;
    double offFumCo = -19.4;
    double defPassCo = -0.62;
    double defRunCo = -0.25;
    double penRate = -1.53;

    @Id
    @GeneratedValue
    Integer id;

    // Team name
    @Column(nullable = false, unique = true)
    String name;

    // Offensive pass yards minus sack yards per attempt
    @Column(nullable = false)
    Double offPass;

    // Offensive rush yards per attempt
    @Column(nullable = false)
    Double offRush;

    // Offensive interceptions per attempt
    @Column(nullable = false)
    Double offInterceptions;

    // Offensive fumbles per attempt
    @Column(nullable = false)
    Double offFumbles;

    // Defensive pass yards per attempt
    @Column(nullable = false)
    Double defPass;

    // Defensive rush yards per attempt
    @Column(nullable = false)
    Double defRush;

    // Penalty yards per play
    @Column(nullable = false)
    Double penalty;

    @Column(nullable = true)
    Double teamLogit;

    @Column(nullable = true)
    ArrayList<Integer> opponents;

    @Column(nullable = true)
    Double GWP;

    @Column(nullable = true)
    Double adjustedGWP;

    @Column(nullable = true)
    Double averageOppGWP;

    @Column(nullable = true)
    Double adjustedAverageOppGWP;

    @Column(nullable = true)
    Double adjustedTeamLogit;


    public Team() {
    }

    public Team(String name, Double offPass, Double offRush, Double offInterceptions, Double offFumbles, Double defPass,
                Double defRush, Double penalty, Double teamLogit, ArrayList<Integer> opponents, Double GWP,
                Double adjustedGWP, Double averageOppGWP, Double adjustedAverageOppGWP, Double adjustedTeamLogit) {
        this.name = name;
        this.offPass = offPass;
        this.offRush = offRush;
        this.offInterceptions = offInterceptions;
        this.offFumbles = offFumbles;
        this.defPass = defPass;
        this.defRush = defRush;
        this.penalty = penalty;
        this.teamLogit = teamLogit;
        this.opponents = opponents;
        this.GWP = GWP;
        this.adjustedGWP = adjustedGWP;
        this.averageOppGWP = averageOppGWP;
        this.adjustedAverageOppGWP = adjustedAverageOppGWP;
        this.adjustedTeamLogit = adjustedTeamLogit;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getOffPass() {
        return offPass;
    }

    public void setOffPass(Double offPass) {
        this.offPass = offPass;
    }

    public Double getOffRush() {
        return offRush;
    }

    public void setOffRush(Double offRush) {
        this.offRush = offRush;
    }

    public Double getOffInterceptions() {
        return offInterceptions;
    }

    public void setOffInterceptions(Double offInterceptions) {
        this.offInterceptions = offInterceptions;
    }

    public Double getOffFumbles() {
        return offFumbles;
    }

    public void setOffFumbles(Double offFumbles) {
        this.offFumbles = offFumbles;
    }

    public Double getDefPass() {
        return defPass;
    }

    public void setDefPass(Double defPass) {
        this.defPass = defPass;
    }

    public Double getDefRush() {
        return defRush;
    }

    public void setDefRush(Double defRush) {
        this.defRush = defRush;
    }

    public Double getPenalty() {
        return penalty;
    }

    public void setPenalty(Double penalty) {
        this.penalty = penalty;
    }

    public Double getTeamLogit() {
        return teamLogit;
    }

    public void setTeamLogit(Double teamLogit) {
        this.teamLogit = teamLogit;
    }

    public ArrayList<Integer> getOpponents() {
        return opponents;
    }

    public void setOpponents(ArrayList<Integer> opponents) {
        this.opponents = opponents;
    }

    public Double getGWP() {
        return GWP;
    }

    public void setGWP(Double GWP) {
        this.GWP = GWP;
    }

    public Double getAdjustedGWP() {
        return adjustedGWP;
    }

    public void setAdjustedGWP(Double adjustedGWP) {
        this.adjustedGWP = adjustedGWP;
    }

    public Double getAverageOppGWP() {
        return averageOppGWP;
    }

    public void setAverageOppGWP(Double averageOppGWP) {
        this.averageOppGWP = averageOppGWP;
    }

    public Double getAdjustedAverageOppGWP() {
        return adjustedAverageOppGWP;
    }

    public void setAdjustedAverageOppGWP(Double adjustedAverageOppGWP) {
        this.adjustedAverageOppGWP = adjustedAverageOppGWP;
    }

    public Double getAdjustedTeamLogit() {
        return adjustedTeamLogit;
    }

    public void setAdjustedTeamLogit(Double adjustedTeamLogit) {
        this.adjustedTeamLogit = adjustedTeamLogit;
    }

    // first step correct
    public double computeTeamLogit(double offPass, double offRush, double offInterception, double offFumbles,
                                   double defPass, double defRush, double penalty) {
        double teamlogit = 0;
        teamlogit = offPassCo * offPass + offRunCo * offRush + offIntCo * offInterception + offFumCo * offFumbles +
                defPassCo * defPass + defRunCo * defRush + penRate * penalty;
        return teamlogit;
    }

    public double computeFinalLogit(double homeLogit, double homeOppLogit, double awayLogit, double awayOppLogit) {
        double ultimateLogit = 0;
        ultimateLogit = constant + homeField + (homeLogit + homeOppLogit) - (awayLogit + awayOppLogit);
        return ultimateLogit;
    }

    // third step correct
    public double computeOdds(double logit) {
        double odds = 0;
        odds = Math.pow(Math.E, logit);
        return odds;
    }

    // fourth step correct
    // now have GWP
    public double computeProbability(double odds) {
        double probability = 0;
        probability = odds / (1 + odds);
        return probability;
    }

    // second step correct
    public double computeGWPLogit(double teamLogit, double averageLogit) {
        double gwpLogit = 0;
        gwpLogit = teamLogit - averageLogit;
        return gwpLogit;
    }

    public double computeOddsFromProb(double averageOppGWP) {
        double odds = 0;
        odds = averageOppGWP / (1 - averageOppGWP);
        return odds;
    }

    // Average opp. gwp logit
    public double computeLogitFromOdds(double odds) {
        double logit = 0;
        logit = Math.log(odds);
        return logit;
    }

    public double computeAdjustedGWPLogit(double teamLogit, double averageLogit, double averageOppGWPLogit) {
        double adjustedGWPLogit = 0;
        adjustedGWPLogit = teamLogit - averageLogit + averageOppGWPLogit;
        return adjustedGWPLogit;
    }
}

