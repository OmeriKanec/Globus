package module13;

import java.io.IOException;

public final class ResourceDemo {
    private static final class Connection implements AutoCloseable {
        private Connection() {
            System.out.println("Open connection");
        }

        private Session openSession() {
            return new Session();
        }

        @Override
        public void close() {
            System.out.println("Close connection");
        }
    }

    private static final class Session implements AutoCloseable {
        private Session() {
            System.out.println("Open session");
        }

        private void execute() throws IOException {
            System.out.println("Execute session command");
            throw new IOException("Session command failed");
        }

        @Override
        public void close() throws IOException {
            System.out.println("Close session");
            throw new IOException("Session close failed");
        }
    }

    public static void main(String[] args) {
        try (Connection connection = new Connection();
             Session session = connection.openSession()) {
            session.execute();
        } catch (IOException primary) {
            System.out.println("Primary: " + primary);
            for (Throwable suppressed : primary.getSuppressed()) {
                System.out.println("Suppressed: " + suppressed);
            }
        }
    }
}
//Исключение из тела try основное, если в процессе закрытия ресурсов возникает ещё одно, то оно присоеденяется к основному как подавленное(это сделано чтобы не перекрыть изначальную причину падания кода)
//Если try не упал, то подавленные исключения могут появиться при падении нескольких закрытий, оставшиеся присоединяются к первому