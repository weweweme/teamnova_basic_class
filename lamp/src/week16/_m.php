<?php
require_once __DIR__ . '/includes/db.php';
require_once __DIR__ . '/includes/util.php';
header('Content-Type: text/plain');
if (isset($_GET['reset'])) { db()->query('DELETE FROM sessions'); echo "reset "; }
echo db()->query("SELECT COUNT(*) FROM sessions")->fetchColumn(), "행\n";
