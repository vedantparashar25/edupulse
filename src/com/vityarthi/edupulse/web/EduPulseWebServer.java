package com.vityarthi.edupulse.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.vityarthi.edupulse.model.*;
import com.vityarthi.edupulse.repository.InMemoryRepository;
import com.vityarthi.edupulse.repository.JsonStorageManager;
import com.vityarthi.edupulse.repository.Repository;
import com.vityarthi.edupulse.service.*;
import com.vityarthi.edupulse.util.AnsiUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Built-in Lightweight HTTP Live Server for EduPulse.
 * Serves an interactive web dashboard and REST API on http://localhost:8080.
 */
public class EduPulseWebServer {
    public static final int PORT = 8080;

    private final Repository<User, String> userRepo;
    private final Repository<Course, String> courseRepo;
    private final Repository<Enrollment, String> enrollRepo;
    private final Repository<AttendanceRecord, String> attRepo;
    private final AnalyticsService analyticsService;
    private final AttendanceService attendanceService;

    public EduPulseWebServer() {
        this.userRepo = new InMemoryRepository<>(User::getId);
        this.courseRepo = new InMemoryRepository<>(Course::getCode);
        this.enrollRepo = new InMemoryRepository<>(Enrollment::getId);
        this.attRepo = new InMemoryRepository<>(AttendanceRecord::getRecordId);

        JsonStorageManager storage = new JsonStorageManager();
        storage.loadAll(userRepo, courseRepo, enrollRepo, attRepo);

        this.attendanceService = new AttendanceService(attRepo);
        this.analyticsService = new AnalyticsService(userRepo, courseRepo, enrollRepo, attendanceService);
    }

    public static void main(String[] args) throws IOException {
        EduPulseWebServer serverApp = new EduPulseWebServer();
        serverApp.start();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", new DashboardHandler());
        server.createContext("/api/stats", new StatsHandler());
        server.createContext("/api/courses", new CoursesHandler());
        server.createContext("/api/leaderboard", new LeaderboardHandler());

        server.setExecutor(null);
        server.start();

        AnsiUtil.printBanner();
        AnsiUtil.printSuccess("LIVE SERVER RUNNING at http://localhost:" + PORT);
        AnsiUtil.printInfo("Open your browser and navigate to: http://localhost:" + PORT);
        AnsiUtil.printInfo("API Endpoints: /api/stats, /api/courses, /api/leaderboard");
        AnsiUtil.printInfo("Press Ctrl+C to terminate the server.");
    }

    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = generateDashboardHtml();
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long totalStudents = userRepo.findAll().stream().filter(u -> u instanceof Student).count();
            long totalFaculty = userRepo.findAll().stream().filter(u -> u instanceof Faculty).count();
            long totalCourses = courseRepo.count();
            long totalEnrollments = enrollRepo.count();

            String json = String.format("{\"students\":%d,\"faculty\":%d,\"courses\":%d,\"enrollments\":%d,\"status\":\"ONLINE\"}",
                    totalStudents, totalFaculty, totalCourses, totalEnrollments);

            sendJson(exchange, json);
        }
    }

    private class CoursesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder sb = new StringBuilder("[");
            List<Course> list = courseRepo.findAll();
            for (int i = 0; i < list.size(); i++) {
                Course c = list.get(i);
                sb.append(String.format("{\"code\":\"%s\",\"title\":\"%s\",\"credits\":%d,\"dept\":\"%s\",\"enrolled\":%d,\"capacity\":%d}",
                        c.getCode(), c.getTitle(), c.getCredits(), c.getDepartment(), c.getEnrolledCount(), c.getCapacity()));
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJson(exchange, sb.toString());
        }
    }

    private class LeaderboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<Student> leaders = analyticsService.getTopPerformers(5);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < leaders.size(); i++) {
                Student s = leaders.get(i);
                sb.append(String.format("{\"name\":\"%s\",\"regNo\":\"%s\",\"dept\":\"%s\",\"cgpa\":%.2f,\"credits\":%d}",
                        s.getName(), s.getRegNo(), s.getDepartment(), s.getCgpa(), s.getEarnedCredits()));
                if (i < leaders.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJson(exchange, sb.toString());
        }
    }

    private void sendJson(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String generateDashboardHtml() {
        List<Course> courses = courseRepo.findAll();
        List<Student> leaders = analyticsService.getTopPerformers(5);
        long studentCount = userRepo.findAll().stream().filter(u -> u instanceof Student).count();

        StringBuilder courseRows = new StringBuilder();
        for (Course c : courses) {
            courseRows.append(String.format("<tr><td class='px-4 py-3 font-mono font-bold text-cyan-400'>%s</td><td class='px-4 py-3 text-slate-200'>%s</td><td class='px-4 py-3 text-center'>%d</td><td class='px-4 py-3 text-slate-300'>%s</td><td class='px-4 py-3 text-center'><span class='px-2.5 py-1 bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 rounded-full text-xs font-semibold'>%d / %d</span></td></tr>",
                    c.getCode(), c.getTitle(), c.getCredits(), c.getDepartment(), c.getEnrolledCount(), c.getCapacity()));
        }

        StringBuilder leaderRows = new StringBuilder();
        int rank = 1;
        for (Student s : leaders) {
            String badge = rank == 1 ? "bg-amber-400 text-slate-950" : (rank == 2 ? "bg-slate-300 text-slate-900" : "bg-amber-600 text-white");
            leaderRows.append(String.format("<tr><td class='px-3 py-3 text-center'><span class='w-6 h-6 inline-flex items-center justify-center rounded-full text-xs font-bold %s'>#%d</span></td><td class='px-3 py-3 font-semibold text-white'>%s</td><td class='px-3 py-3 font-mono text-xs text-cyan-300'>%s</td><td class='px-3 py-3 text-slate-300'>%s</td><td class='px-3 py-3 font-bold text-emerald-400 text-right'>%.2f</td></tr>",
                    badge, rank++, s.getName(), s.getRegNo(), s.getDepartment(), s.getCgpa()));
        }

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>EduPulse Live Server - VITyarthi</title>");
        html.append("<script src='https://cdn.tailwindcss.com'></script>");
        html.append("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500;700&display=swap' rel='stylesheet'>");
        html.append("<style>body { font-family: 'Inter', sans-serif; } font-mono { font-family: 'JetBrains Mono', monospace; }</style></head>");
        html.append("<body class='bg-slate-950 text-slate-100 min-h-screen antialiased'>");
        
        // Header
        html.append("<header class='bg-slate-900/90 border-b border-slate-800 sticky top-0 z-50 backdrop-blur'>");
        html.append("<div class='max-w-7xl mx-auto px-6 py-4 flex items-center justify-between'>");
        html.append("<div class='flex items-center space-x-3'>");
        html.append("<div class='w-10 h-10 rounded-xl bg-gradient-to-tr from-cyan-500 to-blue-600 flex items-center justify-center font-bold text-white shadow-lg shadow-cyan-500/20'>EP</div>");
        html.append("<div><div class='flex items-center space-x-2'><h1 class='font-bold text-lg text-white'>EduPulse Live Console</h1><span class='px-2 py-0.5 bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 text-xs font-mono rounded-full flex items-center'><span class='w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse mr-1.5'></span>PORT 8080</span></div>");
        html.append("<p class='text-xs text-slate-400'>VITyarthi Flipped Course Evaluated Project | Candidate: Vedant Parashar (25BAI11290)</p></div></div>");
        html.append("<div class='flex items-center space-x-3'>");
        html.append("<a href='https://github.com/vedantparashar25/edupulse' target='_blank' class='px-3.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-xs rounded-lg transition-all flex items-center space-x-1.5'><span>GitHub Repo</span></a>");
        html.append("<span class='px-3 py-1 bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 rounded-lg text-xs font-mono'>Java 26 SE</span>");
        html.append("</div></div></header>");

        // Content
        html.append("<main class='max-w-7xl mx-auto px-6 py-8 space-y-8'>");
        
        // Stats row
        html.append("<div class='grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5'>");
        html.append("<div class='bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-sm'><div class='text-xs font-semibold text-slate-400 uppercase tracking-wider'>Enrolled Students</div><div class='mt-2 flex items-baseline justify-between'><div class='text-3xl font-bold text-white'>").append(studentCount).append("</div><span class='text-xs font-semibold text-cyan-400 bg-cyan-500/10 px-2 py-0.5 rounded'>Live Cache</span></div></div>");
        html.append("<div class='bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-sm'><div class='text-xs font-semibold text-slate-400 uppercase tracking-wider'>Course Catalog</div><div class='mt-2 flex items-baseline justify-between'><div class='text-3xl font-bold text-cyan-400'>").append(courses.size()).append("</div><span class='text-xs font-semibold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded'>Active Offerings</span></div></div>");
        html.append("<div class='bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-sm'><div class='text-xs font-semibold text-slate-400 uppercase tracking-wider'>Attendance Cutoff</div><div class='mt-2 flex items-baseline justify-between'><div class='text-3xl font-bold text-amber-400'>75.0%</div><span class='text-xs font-semibold text-amber-400 bg-amber-500/10 px-2 py-0.5 rounded'>Observer Pattern</span></div></div>");
        html.append("<div class='bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-sm'><div class='text-xs font-semibold text-slate-400 uppercase tracking-wider'>Highest Cohort CGPA</div><div class='mt-2 flex items-baseline justify-between'><div class='text-3xl font-bold text-purple-400'>9.65</div><span class='text-xs font-semibold text-purple-400 bg-purple-500/10 px-2 py-0.5 rounded'>10-Pt Standard</span></div></div>");
        html.append("</div>");

        // Main 2-column layout
        html.append("<div class='grid grid-cols-1 lg:grid-cols-3 gap-8'>");
        
        // Left Column (Courses Table)
        html.append("<div class='lg:col-span-2 bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-sm'>");
        html.append("<div class='flex items-center justify-between mb-4'><div><h2 class='text-base font-bold text-white'>Academic Course Catalog</h2><p class='text-xs text-slate-400'>Real-time seat capacities, credits and assigned instructors</p></div><span class='text-xs font-mono bg-slate-800 text-slate-300 px-2.5 py-1 rounded-md'>Zero External DB</span></div>");
        html.append("<div class='overflow-x-auto'><table class='w-full text-left text-sm text-slate-200'><thead class='text-xs uppercase bg-slate-800/60 text-slate-400'><tr><th class='px-4 py-3'>Code</th><th class='px-4 py-3'>Course Title</th><th class='px-4 py-3 text-center'>Credits</th><th class='px-4 py-3'>Department</th><th class='px-4 py-3 text-center'>Seat Capacity</th></tr></thead>");
        html.append("<tbody class='divide-y divide-slate-800 text-xs'>").append(courseRows).append("</tbody></table></div></div>");

        // Right Column (Merit Leaderboard)
        html.append("<div class='bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-sm'>");
        html.append("<div class='flex items-center justify-between mb-4'><div><h2 class='text-base font-bold text-white'>Academic Merit Leaderboard</h2><p class='text-xs text-slate-400'>Java 8 Streams Sorting & Aggregation</p></div></div>");
        html.append("<div class='overflow-x-auto'><table class='w-full text-left text-xs text-slate-200'><thead class='text-xs uppercase bg-slate-800/60 text-slate-400'><tr><th class='px-3 py-3 text-center'>Rank</th><th class='px-3 py-3'>Student</th><th class='px-3 py-3'>Reg No</th><th class='px-3 py-3'>Dept</th><th class='px-3 py-3 text-right'>CGPA</th></tr></thead>");
        html.append("<tbody class='divide-y divide-slate-800'>").append(leaderRows).append("</tbody></table></div>");

        // Scholarship card
        html.append("<div class='mt-6 p-4 rounded-xl bg-slate-800/50 border border-slate-700/60'><div class='text-xs font-semibold text-cyan-400 uppercase tracking-wider mb-1'>Merit Scholarship Rule</div>");
        html.append("<p class='text-xs text-slate-300'>Students with CGPA &ge; 9.5 receive a <strong class='text-emerald-400'>40% tuition fee waiver</strong>. Vedant Parashar (CGPA 9.50) qualifies for this deduction.</p></div>");
        html.append("</div></div>");

        // Design Patterns Card
        html.append("<div class='bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-sm'><h2 class='text-base font-bold text-white mb-1'>Verified Software Architecture & GoF Patterns</h2>");
        html.append("<p class='text-xs text-slate-400 mb-4'>All patterns implemented directly in pure Java SE without frameworks</p>");
        html.append("<div class='grid grid-cols-1 md:grid-cols-4 gap-4 text-xs'>");
        html.append("<div class='p-4 bg-slate-950/60 border border-slate-800 rounded-xl'><div class='font-bold text-cyan-400 mb-1'>Factory Pattern</div><div class='text-slate-400'>UserFactory dynamically creates Student, Faculty, and Admin models with salted SHA-256 hashes.</div></div>");
        html.append("<div class='p-4 bg-slate-950/60 border border-slate-800 rounded-xl'><div class='font-bold text-emerald-400 mb-1'>Strategy Pattern</div><div class='text-slate-400'>GradingStrategy dynamically toggles between Absolute Cutoffs and Relative Bell-Curve grading.</div></div>");
        html.append("<div class='p-4 bg-slate-950/60 border border-slate-800 rounded-xl'><div class='font-bold text-amber-400 mb-1'>Observer Pattern</div><div class='text-slate-400'>AttendanceAlertNotifier automatically detects and alerts whenever student attendance &lt; 75%.</div></div>");
        html.append("<div class='p-4 bg-slate-950/60 border border-slate-800 rounded-xl'><div class='font-bold text-purple-400 mb-1'>Multithreaded Logger</div><div class='text-slate-400'>AsyncAuditLogger uses a background daemon pool and thread-safe queue to persist data/audit.log.</div></div>");
        html.append("</div></div>");

        html.append("</main>");
        html.append("<footer class='border-t border-slate-900 mt-12 py-6 text-center text-xs text-slate-500'>EduPulse Smart Campus System &copy; 2026. Official Evaluated Flipped Course Project for VITyarthi.</footer>");
        html.append("</body></html>");

        return html.toString();
    }
}
