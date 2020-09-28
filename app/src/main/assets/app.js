var MAX_LIFETIME = 50;       // Max device lifetime (in seconds)
var CONFIDENT_LIFETIME = 10; // Maximum seconds to consider a device is still there
var MIN_DISTANCE = 0.5;      // Distance considered to be at exactly the center (in meters)
var MAX_DISTANCE = 3.5;      // Max device distance from center to outer circle (in meters)

/**
 * Parse beacons
 * @param  {string}   rawBeacons Raw beacons
 * @return {object[]}            Parsed beacons
 */
function parseBeacons(rawBeacons) {
    var res = [];
    rawBeacons.split(';').forEach(function(elem) {
        if (elem.length === 0) return;
        var fields = elem.split('|');
        res.push({
            timestamp: ~~(Number(fields[0]) / 1000),
            distance: Number(fields[1]),
            randomId: fields[2]
        });
    });
    return res;
}


/**
 * Get devices
 * @param  {object[]}              beacons List of beacons
 * @return {object<string,object>}         Map of devices
 */
function getDevices(beacons) {
    var devices = {};
    for (var beacon of beacons) {
        if (typeof devices[beacon.randomId] === 'undefined') {
            devices[beacon.randomId] = {
                distance: Number.POSITIVE_INFINITY,
                lastSeen: 0
            };
        }
        if (beacon.timestamp > devices[beacon.randomId].lastSeen) {
            devices[beacon.randomId].distance = beacon.distance;
            devices[beacon.randomId].lastSeen = beacon.timestamp;
        }
    }
    return devices;
}


/**
 * Polar to cartesian coordinates
 * @param {float} distance Distance (radius)
 * @param {float} angle    Angle (phi)
 */
function polarToCartesian(distance, angle) {
    return {
        x: distance * Math.cos(angle),
        y: distance * Math.sin(angle)
    };
}


/**
 * To linear value
 * @param  {float} value Value
 * @param  {float} min   Minimum value
 * @param  {float} max   Maximum value
 * @return {float}       Value between 0 and 1
 */
function toLinearValue(value, min, max) {
    var normalizedValue = Math.min(Math.max(value-min, 0), max);
    return normalizedValue / max;
}


/**
 * Get pseudo-random integer
 * @param  {int} min Minimum value
 * @param  {int} max Maximum value
 * @return {int}     Random integer
 */
function getPseudoRandomInteger(min, max) {
    return (Math.random() * (max - min + 1) ) << 0;
}


/**
 * Render radar
 * @param {object<string,object>} devices Map of devices
 */
function renderRadar(devices) {
    var container = document.querySelector('.radar .clients');

    // Add active devices
    var now = ~~(Date.now() / 1000);
    var confidentCount = 0;
    var countTolerance = 0;
    for (var randomId in devices) {
        var elem = container.querySelector('.client[data-id="' + randomId + '"]');
        if (elem === null) {
            elem = document.createElement('div');
            elem.classList.add('client');
            elem.dataset.angle = getPseudoRandomInteger(0, 360);
            elem.dataset.id = randomId;
            container.appendChild(elem);
        }

        var elapsedSeconds = now - devices[randomId].lastSeen;
        if (elapsedSeconds <= CONFIDENT_LIFETIME) {
            confidentCount++;
        } else if (elapsedSeconds <= MAX_LIFETIME) {
            countTolerance++;
        }
        elem.style.opacity = toLinearValue(MAX_LIFETIME-elapsedSeconds, 0, MAX_LIFETIME);

        var distance = toLinearValue(devices[randomId].distance, MIN_DISTANCE, MAX_DISTANCE) * 100;
        var point = polarToCartesian(distance, elem.dataset.angle);
        elem.style.left = (point.x+50) + '%';
        elem.style.top = (point.y+50) + '%';
    }

    // Remove lost devices
    container.querySelectorAll('.client').forEach(function(elem) {
        var randomId = elem.dataset.id;
        if (typeof devices[randomId] === 'undefined') {
            elem.style.opacity = 0;
            setTimeout(function() {
                elem.remove();
            }, 300);
        }
    });

    // Render count
    var countText = confidentCount;
    if (countTolerance > 0) {
        countText += '-' + (confidentCount+countTolerance);
    }
    document.querySelector('.people-count .number').innerHTML = countText;
}


window.addEventListener('beacons', function(e) {
    var beacons = parseBeacons(e.detail);
    var devices = getDevices(beacons);
    renderRadar(devices);
});