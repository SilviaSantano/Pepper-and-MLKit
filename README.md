# Pepper and ML Kit
This is an Android Application to enhance Pepper robot with AI by using [Google’s ML Kit SDK](https://developers.google.com/ml-kit).

[Pepper](https://www.aldebaran.com/en/pepper) is a social robot by Aldebaran with a humanoid body shape designed to engage with people through natural language using voice and gestures. Pepper can be programmed to adapt to the human by recognizing faces and basic human emotions, thus creating a sort of bond during an encounter. The interaction is also supported by a touch screen integrated on its chest, through which visual contents can be presented to the user and input can be collected.

This app includes the following four different demos you can start from a menu via voice or tablet.

<img src="https://user-images.githubusercontent.com/12804135/196050816-e89bb360-bc10-4373-90f5-0cf0253dba07.jpg" width="700">

## Demo with ML Kit's object detection API
With this demo, our idea is to leverage object detection to recognize objects in an image and their position so that Pepper can localize them in a room. Pepper should be able to respond to the question of what objects he can see and even point at them when asked to. It is based on ML Kit's [Object detection and tracking API](https://developers.google.com/ml-kit/vision/object-detection).

<img src="https://user-images.githubusercontent.com/12804135/196051173-fa0beb30-daa0-4543-b19b-d3574ba48bcf.jpg" width="700">

## Demo with ML Kit's digital ink recognition API
This demo is a game based on Google’s ML Kit [Digital Ink Recognition API](https://developers.google.com/ml-kit/vision/digital-ink-recognition). With this API we can recognize sketches and handwritten text on a digital surface, what we use to implement the following game: we will draw or write something on the tablet on Pepper’s chest and Pepper should be able to recognize what it is.

## Demo with ML Kit's text recognition API
In this demo, we want to further enhance Pepper’s abilities by teaching it to read text aloud. The goal of this demo is having the possibility to ask Pepper what some text that’s in front of it says, to what it is able to read it by means of OCR (Optical Character Recognition) through the ML Kit [Text Recognition API](https://developers.google.com/ml-kit/vision/text-recognition).

## Demo with ML Kit's translation API
In this demo, you can ask Pepper to translate a word or a sentence between any pair of languages of those available in your robot. Pepper will respond, uttering the translation in the target language by means of the TextToSpeech android library. The translation will be powered by the ML Kit on-device [Translation API](https://developers.google.com/ml-kit/language/translation), which makes use of the same models used by the Google Translate app’s offline mode. 