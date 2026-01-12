/*
 *  Copyright 2022 Silvere Martin-Michiellot
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jants.mapcomponents;

//taken after https://github.com/INBASD/Solar-Power-Project/
public class InsulationCalculator {

        // Solar radiation striking a location

        // insolation is the measure of the ammount of solar radiation striking
        // the ground.  This class gives methods that allow for the calculation (by simulation)
        // of the insolation at any time, allows calculation of the total for
        // any day of the year, and the sum total for any number of years.

        //At the lowest level of calculation, degrees are converted to radians using
        // DegreesToRadians();
        private static int solarRadiation = 1120;//https://en.wikipedia.org/wiki/Solar_irradiance 1120 at ground level
        private static int dailySimulationSteps = 10;

        //given a location latitude, hour of day, and day of year (jan 1 = 1),
        // calculate the solar insulation at that instant.
        //By Jack Williams, n5736153
        public double hourInsulation(double latitude, double hour, int day) {

            double declination;
            double hourangle;
            double zenithangle;
            double insolation;

            hourangle = hourToHourAngle(hour);

            declination = solarDeclination(day);

            zenithangle = zenithAngle(latitude, declination, hourangle);

            insolation = solarRadiation * Math.cos(zenithangle);

            return insolation;

        }

        //given a location and a day, calculate the total insulation of the location for that day
        // ignoring the hours before and after sunset.
        // The total insulation can be approximated first by simulating a set of hours.
        //  then averaging the result to calculate average insulation per hour.
        //  then multiplying this by the number of hours in the day.
        //By Jack Williams, n5736153
        public double dailyInsulation(double latitude, int day) {
            double sunriseHourAngle;
            double declination;
            double sunriseHour;

            // calculate declination
            declination = solarDeclination(day);
            //calculate sunrise

            //sunrise hour angle
            sunriseHourAngle = sunriseHourAngle(latitude, declination);

            //the solar hour, based on the angle
            sunriseHour = -(hourAngleToHour(sunriseHourAngle) - 12);

            double averageHourlyInsolation = 0;

            //the number of hours per simulation step, given number of hours between sunrise and midday.
            double simulationStepLength = (12 + sunriseHour) / dailySimulationSteps;

            for (int i = 1; i <= dailySimulationSteps; i++){

                averageHourlyInsolation = (averageHourlyInsolation + hourInsulation(latitude, (i * simulationStepLength), day)) / 2;
                //System.out.println(averageHourlyInsolation);

            }

            //total daylight hours, time before midday + time after midday is the same as time between sunup and sundown,
            // which is the same as between sunup and midday * 2.
            double totalDaylightHours = -sunriseHour * 2;

            double totalDailyInsulation = totalDaylightHours * averageHourlyInsolation;

            //System.out.println("Total Daylight Hours: " + totalDaylightHours);
            //System.out.println("Average insulation: " + averageHourlyInsulation);

            return totalDailyInsulation;
        }

        //HOUR TO HOUR ANGLE
        // given an hour (midnight = 0, 12 = midday), calculate the hour angle at 15 degrees per hour.
        // returns radians rotated at this hour
        //By Jack Williams, n5736153
        public double hourToHourAngle(double hourFromMidnight) {
            double hourAngle = degreesToRadians(15*(hourFromMidnight - 12));
            return hourAngle;
        }

        //HOUR ANGLE TO HOUR
        // given an hour angle (radians), determine the current hour.
        //By Jack Williams, n5736153
        public double hourAngleToHour(double hourAngle) {
            double hourFromMidnight = (hourAngle / degreesToRadians(15)) + 12;
            return hourFromMidnight;
        }

        //DEGREES TO RADIANS helper function
        // given a degrees, convert to radians.
        //By Jack Williams, n5736153
        public double degreesToRadians(double degrees) {
            double radians = (degrees * Math.PI) / 180;

            return radians;
        }

        //RADIANS TO DEGREES helper function
        // given some radians, convert to degrees.
        //By Jack Williams, n5736153
        public double radiansToDegrees(double radians) {
            double degrees = (radians * 180) / Math.PI;

            return degrees;
        }

        //DETERMINE SOLAR DECLINATION
        // given a day of the year, determine the solar declination.
        //By Jack Williams, n5736153
        public double solarDeclination(int dayOfYear) {
            double declination = degreesToRadians(23.45) * (Math.sin((degreesToRadians(360)/365)*(dayOfYear)));

            return declination;
        }

        //DETERMINE ZENITH ANGLE
        // given a latitude, declination, and Hour Angle, determine the zenith angle
        //  which is the sun's angle from 0 (straight up) on that day.
        //By Jack Williams, n5736153
        public double zenithAngle(double latitude, double declination, double hourAngle) {
            double latitudeRadians = degreesToRadians(latitude);
            double zenithangle = (Math.acos( Math.sin(latitudeRadians) * Math.sin(declination)) + ( Math.cos(latitudeRadians) * Math.cos(declination) * Math.cos(hourAngle)));

            return zenithangle;
        }

        //CALCULATE SUNRISE HOUR ANGLE
        //  Given a latitude and a declination, determine the hour angle at sunrise for that location.
        //By Jack Williams, n5736153
        public double sunriseHourAngle(double latitude, double declination) {
            double latitudeRadians = degreesToRadians(latitude);
            double sunriseHourAngle = (Math.acos(-Math.tan(latitudeRadians) * Math.tan(declination)));

            return sunriseHourAngle;
        }

}
