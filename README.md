# Blockdoku Android

Blockdoku is an Android block puzzle game inspired by Sudoku, developed using Kotlin and Android Studio.

## Overview

Players place differently shaped blocks on a 9×9 board using drag-and-drop controls.
Rows, columns, and 3×3 areas are cleared when completed, and the player earns points based on successful placements and clears.

## Features

- 9×9 puzzle board
- Drag-and-drop block placement
- Row / column / 3×3 area clearing logic
- Score calculation system
- Bomb block mechanic
- Game over detection
- Tutorial UI

## Tech Stack

- Kotlin
- Android Studio
- Android SDK

## Development

This project was developed by a two-person team.

My responsibilities included:
- Game rule design
- Core game logic implementation
- Board state management
- Block placement validation
- Score calculation
- Bomb block logic
- Game over detection

The other contributor focused on:
- Tutorial UI implementation
- UI improvements
- Bug fixes

## Technical Challenges

One of the most challenging parts was determining whether differently shaped blocks could be placed correctly on the board.

To solve this, the board was managed as an 81-cell state array, and block placement was validated using relative coordinates, boundary checks, and collision detection with existing blocks.

## Future Improvements

- Ranking system
- Backend API integration
- Database integration
- Cloud deployment
