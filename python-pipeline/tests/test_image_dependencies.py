"""Exercise the installed OpenCV/NumPy binary interface without mocks."""

import cv2
import numpy as np


def test_opencv_processes_numpy_image():
    image = np.array([[[0, 0, 0], [255, 255, 255]]], dtype=np.uint8)

    grayscale = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    np.testing.assert_array_equal(grayscale, np.array([[0, 255]], dtype=np.uint8))
    assert grayscale.dtype == np.uint8
