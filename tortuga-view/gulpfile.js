var gulp = require('gulp');

var uglify = require('gulp-uglify');
var rename = require('gulp-rename');
var concat = require('gulp-concat');
var inject = require('gulp-inject');
var minifyCss = require('gulp-cssmin');
var gutil = require('gulp-util');
var replace = require('gulp-replace');
var gulpif = require('gulp-if');

var isDevelopment = gutil.env.dev === true;

var paths = {
    libraries: [
        'node_modules/angular/angular.min.js',
        'node_modules/angular-messages/angular-messages.min.js',
        'node_modules/angular-cookies/angular-cookies.min.js',
        'node_modules/angular-animate/angular-animate.min.js',
        'node_modules/angular-aria/angular-aria.min.js',
        'node_modules/base-64/base64.js',
        'node_modules/angular-material/angular-material.min.js',
        'node_modules/angular-ui-router/release/angular-ui-router.min.js',
        'node_modules/angular-route/angular-route.min.js',
        'node_modules/angular-resource/angular-resource.js',
        'node_modules/angular-material-calendar/angular-material-calendar.min.js',
        'node_modules/angular-sanitize/angular-sanitize.min.js',
        'node_modules/chart.js/Chart.js',
        'node_modules/angular-chart.js/dist/angular-chart.js',
        'lib/ngsweets.js',
        'lib/twemoji.min.js'
    ],
    scripts: [
        'app.js',

        'src/management/management.js',
        'src/management/users/users.js',
        'src/management/devices/device.js',
        'src/management/admin/admin.js',
        'src/management/reservations/reservations.js',
        'src/management/statistics/statistics.js',
        'src/home/home.js',
        'src/login/login.js',
        'src/room/room.js',
        'stc/newPin/newPin.js',
        'src/tickets/tickets.js',
        'src/tickets/roomReservation/roomReservation.js',
        'src/tickets/support/support.js',
        'src/support/supportTicket.js',
        'src/profile/profile.js',
        'src/deviceReservations/deviceReservations.js',
        'src/calendar/calendar.js',
        'src/import/import.js',

        'src/**/*.js'
    ],
    styles: [
        'node_modules/angular-material/angular-material.min.css',
        'node_modules/angular-chart.js/dist/angular-chart.css',
        'lib/animate.css',
        'lib/angular-chart.js/angular-chart.min.css',
        'style.dev.css'
    ]
};

gulp.task('scripts', function() {
    var scripts = gulp.src(paths.libraries.concat(paths.scripts))
        .pipe(gulpif(!isDevelopment,
            gulpif(function(file) {
                return file.path.indexOf('lib/') == -1;
            }, uglify())
        ))
        .pipe(concat('app.js'))
        .pipe(gulp.dest('dist/'));

    return gulp.src('./index.dev.html')
        .pipe(inject(scripts))
        .pipe(rename('index.html'))
        .pipe(gulp.dest('./'));
});

gulp.task('styles', function() {
    return gulp.src(paths.styles)
        .pipe(concat('style.css'))
        .pipe(gulpif(!isDevelopment, minifyCss()))
        .pipe(gulp.dest('dist/'));
});

gulp.task('terminal', function() {
    var host = process.env.BASE_HOST;

    if(host == undefined) {
        host = 'http://localhost:2222/';
    } else {
        console.log("Using " + host + " as the host");
    }

    return gulp.src('./terminal/index.dev.html')
        .pipe(replace('$$HOST', host))
        .pipe(rename('index.html'))
        .pipe(gulp.dest('./terminal/'));
});

gulp.task('build', ['scripts', 'terminal', 'styles']);

gulp.task('watch', function(){
    gulp.watch('src/**/*.js',['scripts']);
    gulp.watch('index.dev.html', ['scripts']);
    gulp.watch(['src/**/*.css', 'style.dev.css'], ['styles']);
    gulp.watch('./terminal/index.dev.html', ['terminal']);
});

gulp.task('default', ['build', 'watch']);