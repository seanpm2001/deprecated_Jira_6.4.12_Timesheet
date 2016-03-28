/*
 * Copyright 2016 Adrian Schnedlitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.catrobat.jira.timesheet.helper;

import org.catrobat.jira.timesheet.activeobjects.*;

import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;

public abstract class CsvTimesheetExporter {

    public static final String DELIMITER = ";";
    public static final String NEW_LINE = "\n";

    private final ConfigService configService;

    public CsvTimesheetExporter(ConfigService configService) {
        this.configService = configService;
    }

    public String getTimesheetCsvData(Timesheet timesheet) {
        return fetchTimesheetData(timesheet);
    }

    public String getTimesheetCsvDataAll(List<Timesheet> timesheetList) {
        return fetchTimesheetDataAll(timesheetList);
    }

    public String getConfigCsvData(Config config) {
        return fetchConfigData(config);
    }

    private String fetchConfigData(Config config) {
        StringBuilder sb = new StringBuilder();

        sb.append("Date" + DELIMITER);
        sb.append(new Date()).append(NEW_LINE);

        //Approved Users
        sb.append("Approved Users" + DELIMITER);
        for (ApprovedUser approvedUser : config.getApprovedUsers())
            sb.append(approvedUser.getUserName()).append(DELIMITER);
        sb.append(NEW_LINE);

        //Email Notifications
        sb.append("Email Settings General" + DELIMITER);
        sb.append(NEW_LINE);
        sb.append("Email From Name" + DELIMITER);
        sb.append(config.getMailFromName()).append(DELIMITER);
        sb.append("Email From Mail-Address" + DELIMITER);
        sb.append(config.getMailFrom()).append(DELIMITER);
        sb.append(NEW_LINE);

        //Email Out Of Time
        sb.append("Email Out of Time" + DELIMITER);
        sb.append(NEW_LINE);
        sb.append("Email Out Of Time Subject" + DELIMITER);
        sb.append(unescape(config.getMailSubjectTime())).append(DELIMITER);
        sb.append("Email Out Of Time Body" + DELIMITER);
        sb.append(unescape(config.getMailBodyTime())).append(DELIMITER);
        sb.append(NEW_LINE);

        //Email Inactive
        sb.append("Email Inactive" + DELIMITER);
        sb.append(NEW_LINE);
        sb.append("Email Inactive Subject" + DELIMITER);
        sb.append(unescape(config.getMailSubjectInactive())).append(DELIMITER);
        sb.append("Email Inactive Body" + DELIMITER);
        sb.append(unescape(config.getMailBodyInactive())).append(DELIMITER);
        sb.append(NEW_LINE);

        //Email Admin
        sb.append("Email Admin Changed Entry" + DELIMITER);
        sb.append(NEW_LINE);
        sb.append("Email Admin Changed Entry Subject" + DELIMITER);
        sb.append(unescape(config.getMailSubjectEntry())).append(DELIMITER);
        sb.append("Email Admin Changed Entry Body" + DELIMITER);
        sb.append(unescape(config.getMailBodyEntry())).append(DELIMITER);
        sb.append(NEW_LINE);

        //Teams
        sb.append("Teams" + NEW_LINE);
        for (Team team : config.getTeams()) {
            sb.append(NEW_LINE);
            sb.append("Team Name" + DELIMITER);
            sb.append(unescape(team.getTeamName())).append(DELIMITER + NEW_LINE);
            //Append Coordinoators
            sb.append("Assigned Coordinators" + DELIMITER);
            for (String userName : configService.getGroupsForRole(team.getTeamName(), TeamToGroup.Role.COORDINATOR))
                sb.append(unescape(userName)).append(DELIMITER);
            sb.append(NEW_LINE);
            //Append Users
            sb.append("Assigned Users" + DELIMITER);
            for (String userName : configService.getGroupsForRole(team.getTeamName(), TeamToGroup.Role.DEVELOPER))
                sb.append(unescape(userName)).append(DELIMITER);
            sb.append(NEW_LINE);
            //Append Categories
            sb.append("Assigned Categories" + DELIMITER);
            for (String categoryName : configService.getCategoryNamesForTeam(team.getTeamName()))
                sb.append(unescape(categoryName)).append(DELIMITER);
            sb.append(NEW_LINE);
        }
        return sb.toString();
    }

    private String fetchTimesheetDataAll(List<Timesheet> timesheetList) {
        String timesheetData = "";
        for (Timesheet timesheet : timesheetList) {
            timesheetData = timesheetData + fetchTimesheetData(timesheet);
        }
        return timesheetData;
    }

    private String fetchTimesheetData(Timesheet timesheet) {
        StringBuilder sb = new StringBuilder();

        sb.append("Username" + DELIMITER +
                "Practical Hours" + DELIMITER +
                "Theory Hours" + DELIMITER +
                "Hours Done" + DELIMITER +
                "Substracted Hours" + DELIMITER +
                "Total Hours" + DELIMITER +
                "Remaining Hours" + DELIMITER +
                "Penalty Text" + DELIMITER +
                "ECTS" + DELIMITER +
                "Lecture" + NEW_LINE);

        sb.append(timesheet.getUserKey()).append(DELIMITER);
        sb.append(Integer.toString(timesheet.getTargetHoursPractice())).append(DELIMITER);
        sb.append(Integer.toString(timesheet.getTargetHoursTheory())).append(DELIMITER);
        sb.append(Integer.toString(timesheet.getTargetHoursCompleted())).append(DELIMITER);
        sb.append(Integer.toString(timesheet.getTargetHoursRemoved())).append(DELIMITER);
        sb.append(Integer.toString(timesheet.getTargetHours())).append(DELIMITER);
        sb.append(Integer.toString(timesheet.getTargetHours() - timesheet.getTargetHoursCompleted())).append(DELIMITER);
        sb.append(timesheet.getReason()).append(DELIMITER);
        sb.append(Integer.toString(timesheet.getEcts())).append(DELIMITER);
        sb.append(timesheet.getLectures()).append(NEW_LINE);

        for (TimesheetEntry timesheetEntry : timesheet.getEntries()) {
            sb.append("Begin Date" + DELIMITER +
                    "End Date" + DELIMITER +
                    "Pause Minutes" + DELIMITER +
                    "Duration Minutes" + DELIMITER +
                    "Team" + DELIMITER +
                    "Category" + DELIMITER +
                    "Description" + NEW_LINE);

            Integer hours = 0;
            Integer minutes = timesheetEntry.getDurationMinutes();

            while (minutes - 60 >= 0) {
                minutes = minutes - 60;
                hours++;
            }
            String duration = hours + ":" + minutes;

            sb.append(unescape(timesheetEntry.getBeginDate().toString())).append(DELIMITER);
            sb.append(unescape(timesheetEntry.getEndDate().toString())).append(DELIMITER);
            sb.append(unescape(Integer.toString(timesheetEntry.getPauseMinutes()))).append(DELIMITER);
            sb.append(unescape(duration)).append(DELIMITER);
            sb.append(unescape(timesheetEntry.getTeam().getTeamName())).append(DELIMITER);
            sb.append(unescape(timesheetEntry.getCategory().getName())).append(DELIMITER);
            sb.append(unescape(timesheetEntry.getDescription().toString())).append(NEW_LINE);
        }
        sb.append(NEW_LINE);

        return sb.toString();
    }

    private String unescape(String escapedHtml4String) {
        if (escapedHtml4String == null || escapedHtml4String.trim().length() == 0) {
            return "\"\"";
        } else return "\"" + unescapeHtml4(escapedHtml4String).replaceAll("\"", "\"\"") + "\"";
    }
}
