package backend.api;
import backend.api.config.applicationConfig.Properties;
import backend.api.config.applicationConfig.Server;
import backend.api.config.databaseConfig.DatabaseManager;
import backend.api.exception.CustomException;
import backend.api.service.StatisticsService;

import backend.api.exception.Logger;

public class OCo {

    public static void main(String[] args) {
        try
        {
            run();
            Logger.success("Aplicația a fost pornită cu succes");
        }
        //eroare la pornirea serverului
        catch (CustomException e) {
            Logger.exception(e.getNume());
            Logger.error("Eroare la pornirea aplicației: " + e.getDescriere());
        } catch (Exception e) {
            Logger.exception("UnexpectedException");
            Logger.error("Eroare neașteptată la pornirea aplicației: " + e.getMessage());
        }
    }

    public static void run() throws CustomException {
        try {
            Logger.info("Pornire server...");


            Logger.info("Inițializez baza de date PostgreSQL...");

            //[CLASA PRORPRIE]: configurare baza de date
            DatabaseManager database = DatabaseManager.getInstance();

            database.configDatabase(
                    Properties.getDatabaseUrl(),
                    Properties.getDatabaseUser(),
                    Properties.getDatabasePassword(),
                    Properties.getDatabaseDriver()
            );
            DatabaseManager.initialize();

            Logger.success("Baza de date a fost inițializată cu succes");


            Logger.info("Actualizare flux RSS...");
            //[CLASA PRORPRIE]: actualizare flux RSS
            StatisticsService statisticsService = new StatisticsService();
            statisticsService.getGeneralStatistics();
            Logger.success("Fluxul RSS a fost actualizat cu succes");

            Logger.info("Pornire server HTTP...");
            //[CLASA PRORPRIE]: configurare setari server(adresa, port, endpoint-uri acceptate)
            Server serverManager = new Server();
            serverManager.start(Properties.getAddress(), Properties.getPort());

            Logger.success("Serverul a fost pornit cu succes pe adresa " +
            Properties.getAddress() + ":" + Properties.getPort());
            Logger.info("Link: "+"http://"+Properties.getAddress()+":"+Properties.getPort());

        }
        //erori cunoscute
        catch (CustomException e) {
            throw e;
        }
        //erori necunoscute
        catch (Exception e) {
            throw new CustomException("ServerStartError", "Eroare neașteptată la pornirea serverului", e);
        }
    }
}